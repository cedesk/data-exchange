/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.service.DirectoryWatchService;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by D.Knoll on 09.07.2015.
 */
public class ExternalModelFileWatcher extends Observable {

    private static Logger logger = Logger.getLogger(ExternalModelFileWatcher.class);

    private DirectoryWatchService directoryWatchService;
    private Map<File, ExternalModel> watchedExternalModels = new ConcurrentHashMap<>();
    private Set<File> maskedFiles = new ConcurrentSkipListSet<>();


    public void setDirectoryWatchService(DirectoryWatchService directoryWatchService) {
        this.directoryWatchService = directoryWatchService;
    }

    public void add(ExternalModel externalModel) throws ExternalModelException {
        File cacheFile = externalModel.getCacheFile();
        watchedExternalModels.put(cacheFile, externalModel);
        String filePattern = cacheFile.getName();
        try {
            directoryWatchService.register(new FileChangeListener(), cacheFile.getParent(), filePattern);
        } catch (IOException e) {
            logger.error("unable to observe file " + cacheFile.getAbsolutePath() + " for modifications.");
        }
    }

    public void clear() {
        directoryWatchService.clear();
        watchedExternalModels.clear();
    }

    public void close() {
        directoryWatchService.stop();
    }

    public void maskChangesTo(File file) {
        this.maskedFiles.add(file);
    }

    public void start() {
        directoryWatchService.start();
    }

    public void unmaskChangesTo(File file) {
        this.maskedFiles.remove(file);
    }

    public void onFileModify(File changedFile) {
        if (maskedFiles.contains(changedFile)) {
            return;
        }
        String changedFilePath = changedFile.getAbsolutePath();
        if (watchedExternalModels.containsKey(changedFile)) {
            ExternalModel externalModel = watchedExternalModels.get(changedFile);
            long lastModified = changedFile.lastModified();
            String dateAndTime = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(lastModified));
            logger.debug("file " + changedFilePath + " has been modified (" + dateAndTime + ")");
            // TODO: iif necessary
            try {
                ExternalModelFileWatcher.this.setChanged();
                ExternalModelFileWatcher.this.notifyObservers(externalModel);
            } catch (Exception ex) {
                logger.error("error in notifying file change listeners", ex);
            }
        } else {
            logger.debug("ignoring change on file: " + changedFilePath);
        }
    }

    private class FileChangeListener implements DirectoryWatchService.OnFileChangeListener {
        @Override
        public void onFileModify(File changedFile) {
            ExternalModelFileWatcher.this.onFileModify(changedFile);
        }
    }
}
