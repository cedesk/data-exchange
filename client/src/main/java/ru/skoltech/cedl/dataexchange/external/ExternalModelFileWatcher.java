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
import ru.skoltech.cedl.dataexchange.entity.ExternalModelTreeIterator;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.DirectoryWatchService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

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

    public void add(SystemModel systemModel, Predicate<ModelNode> accessChecker) {
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(systemModel, accessChecker);
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            this.add(externalModel);
        }
    }

    public void add(ExternalModel externalModel) {
        ExternalModelState cacheState = externalModel.state();
        if (cacheState.isCached()) {
            logger.warn("Cannot add to directory watch service external model " + externalModel.getNodePath()
                    + " because of wrong state: " + cacheState);
            return;
        }
        File cacheFile = externalModel.getCacheFile();
        watchedExternalModels.put(cacheFile, externalModel);
        String filePattern = cacheFile.getName();
        try {
            directoryWatchService.register(new FileChangeListener(), cacheFile.getParent(), filePattern);
            this.onFileModify(cacheFile);
        } catch (IOException e) {
            logger.error("Unable to observe file " + cacheFile.getAbsolutePath() + " for modifications.");
        }
    }

    public void clear() {
        directoryWatchService.clear();
        watchedExternalModels.clear();
    }

    public void start() {
        directoryWatchService.start();
    }

    public void close() {
        directoryWatchService.stop();
    }

    public void maskChangesTo(File file) {
        this.maskedFiles.add(file);
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
            logger.debug("File " + changedFilePath + " has been modified (" + dateAndTime + ")");

            if (changedFile.lastModified() > externalModel.getLastModification()) {
                ExternalModelFileWatcher.this.setChanged();
                ExternalModelFileWatcher.this.notifyObservers(externalModel);
            }
        } else {
            logger.debug("Ignoring change on file: " + changedFilePath);
        }
    }

    private class FileChangeListener implements DirectoryWatchService.OnFileChangeListener {
        @Override
        public void onFileModify(File changedFile) {
            ExternalModelFileWatcher.this.onFileModify(changedFile);
        }
    }
}
