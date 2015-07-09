package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.file.DirectoryWatchService;
import ru.skoltech.cedl.dataexchange.file.SimpleDirectoryWatchService;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by D.Knoll on 09.07.2015.
 */
public class ExternalModelFileWatcher extends Observable {

    private static Logger logger = Logger.getLogger(ExternalModelFileWatcher.class);

    private Map<File, ExternalModel> watchedExternalModels = new ConcurrentHashMap<>();

    public ExternalModelFileWatcher() {
        SimpleDirectoryWatchService.getInstance().start();
    }

    public void add(ExternalModel externalModel) {
        File file = ExternalModelFileUtil.getFilePathInCache(externalModel);
        watchedExternalModels.put(file, externalModel);
        String filePattern = file.getName();
        try {
            SimpleDirectoryWatchService.getInstance().register(new FileChangeListener(), file.getParent(), filePattern);
        } catch (IOException e) {
            logger.error("unable to observe file " + file.getAbsolutePath() + " for modifications.");
        }
    }

    public void clear() {
        watchedExternalModels.clear();
        // TODO: remove FileChangeListeners from SimpleDirectoryWatchService
    }

    public void close() {
        SimpleDirectoryWatchService.getInstance().stop();
    }

    private class FileChangeListener implements DirectoryWatchService.OnFileChangeListener {
        @Override
        public void onFileModify(File changedFile) {
            String changedFilePath = changedFile.getAbsolutePath();
            if (watchedExternalModels.containsKey(changedFile)) {
                ExternalModel externalModel = watchedExternalModels.get(changedFile);
                long lastModified = changedFile.lastModified();
                String dateAndTime = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(lastModified));
                logger.debug("file " + changedFilePath + " has been modified (" + dateAndTime + ")");
                // TODO: iif necessary
                setChanged();
                notifyObservers(externalModel);
            } else {
                logger.debug("ignoring change on file: " + changedFilePath);
            }
        }
    }
}
