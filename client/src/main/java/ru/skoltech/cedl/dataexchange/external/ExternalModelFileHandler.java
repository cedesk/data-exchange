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
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;
import ru.skoltech.cedl.dataexchange.service.ExternalModelFileStorageService;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.update.ModelUpdateHandler;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelFileHandler {

    private static Logger logger = Logger.getLogger(ExternalModelFileHandler.class);

    private Project project;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private ModelUpdateHandler modelUpdateHandler;
    private FileStorageService fileStorageService;
    private ExternalModelFileStorageService externalModelFileStorageService;

    private Set<ExternalModel> changedExternalModels = new HashSet<>();

    public void setProject(Project project) {
        this.project = project;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setModelUpdateHandler(ModelUpdateHandler modelUpdateHandler) {
        this.modelUpdateHandler = modelUpdateHandler;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setExternalModelFileStorageService(ExternalModelFileStorageService externalModelFileStorageService) {
        this.externalModelFileStorageService = externalModelFileStorageService;
    }

    public void initializeStateOfExternalModels(SystemModel systemModel, Predicate<ModelNode> accessChecker) {
        externalModelFileWatcher.clear();
        changedExternalModels.clear();
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(systemModel, accessChecker);
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            ModelNode modelNode = externalModel.getParent();

            // check cache state and add file watcher if cached
            ExternalModelCacheState cacheState = this.getCacheState(externalModel);
            if (cacheState != ExternalModelCacheState.NOT_CACHED) {
                File projectDataDir = project.getProjectDataDir();
                File cacheFile = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
                externalModelFileWatcher.add(externalModel, cacheFile);
            }

            // keep track of changed files
            if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT) {
                changedExternalModels.add(externalModel);
                logger.debug(modelNode.getNodePath() + " external model '" + externalModel.getName() + "' has been changed since last store to repository");
            } else if (cacheState == ExternalModelCacheState.CACHED_CONFLICTING_CHANGES) {
                // TODO: WARN USER
                logger.warn(modelNode.getNodePath() + " external model '" + externalModel.getName() + "' has conflicting changes locally and in repository");
            }

            // silently update model from external model
            modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel);
        }
    }

    public void exportValuesToExternalModels(SystemModel systemModel, Predicate<ModelNode> accessChecker) {
        Iterator<ExternalModel> externalModelsIterator = new ExternalModelTreeIterator(systemModel, accessChecker);
        while (externalModelsIterator.hasNext()) {
            ExternalModel externalModel = externalModelsIterator.next();
            try {
                modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
            } catch (ExternalModelException e) {
                logger.warn("Cannot apply parameter updates to ExternalModel: " + externalModel.getNodePath(), e);
            }
        }
    }

    public void addChangedExternalModel(ExternalModel externalModel) {
        changedExternalModels.add(externalModel);
    }

    /**
     * check the locally cached external model files for modifications,
     * and if there are modifications, update the local study model in memory.
     */
    public void updateExternalModelsInStudy() {
        for (ExternalModel externalModel : changedExternalModels) {
            ExternalModelCacheState cacheState = this.getCacheState(externalModel);
            if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT) {
                logger.debug("updating " + externalModel.getNodePath() + " from file");
                try {
                    File projectDataDir = project.getProjectDataDir();
                    File file = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
                    externalModelFileStorageService.readExternalModelAttachmentFromFile(file, externalModel);
                } catch (IOException e) {
                    logger.error("error updating external model from file!", e);
                }
            } else if (cacheState == ExternalModelCacheState.CACHED_CONFLICTING_CHANGES) {
                // TODO: WARN USER, PROVIDE WITH CHOICE TO REVERT OR FORCE CHECKIN
                logger.warn(externalModel.getNodePath() + " has conflicting changes locally and in repository");
            } else {
                logger.warn(externalModel.getNodePath() + " is in state " + cacheState);
            }
        }
    }

    /**
     * make sure external model files in cache get a new timestamp
     */
    public void updateExternalModelStateInCache() {
        for (ExternalModel externalModel : changedExternalModels) {
            ExternalModelCacheState cacheState = this.getCacheState(externalModel);
            if (cacheState != ExternalModelCacheState.NOT_CACHED) {
                logger.debug("timestamping " + externalModel.getNodePath());
                this.updateCheckoutTimestamp(externalModel);
                if (logger.isDebugEnabled()) {
                    String modelModification = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(externalModel.getLastModification()));
                    long checkoutTime = this.getCheckoutTime(externalModel);
                    String fileModification = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(checkoutTime));
                    logger.debug("stored external model '" + externalModel.getName() +
                            "' (model: " + modelModification + ", file: " + fileModification + ")");
                    logger.debug(externalModel.getNodePath() + " is now in state " + cacheState);
                }
            }
        }
        changedExternalModels.clear();
    }


    public ExternalModelCacheState getCacheState(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        File projectDataDir = project.getProjectDataDir();
        File modelFile = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
        Long modelLastStored = externalModel.getLastModification();
        if (modelFile.exists() && modelLastStored != null) {
            long checkoutTime = this.getCheckoutTime(modelFile);
            long fileLastModified = modelFile.lastModified();
            boolean newerInRepository = modelLastStored > checkoutTime;
            boolean locallyModified = checkoutTime < fileLastModified;
            if (newerInRepository) {
                if (locallyModified) {
                    return ExternalModelCacheState.CACHED_CONFLICTING_CHANGES;
                } else {
                    return ExternalModelCacheState.CACHED_OUTDATED;
                }
            } else {
                if (locallyModified) {
                    return ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT;
                } else {
                    return ExternalModelCacheState.CACHED_UP_TO_DATE;
                }
            }
        }
        return ExternalModelCacheState.NOT_CACHED;
    }

    private long getCheckoutTime(File file) {
        File tsFile = getTimestampFile(file);
        if (!tsFile.exists()) {
            logger.error("external model is missing checkout timestamp");
        }
        return tsFile.lastModified();
    }

    private File getTimestampFile(File file) {
        return new File(file.getAbsolutePath() + ".tstamp");
    }

    private void updateCheckoutTimestamp(ExternalModel externalModel) {
        File projectDataDir = project.getProjectDataDir();
        File file = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
        File tsFile = getTimestampFile(file);
        try {
            if (!tsFile.exists()) {
                tsFile.createNewFile(); // create file marking the checkout time of the ExternalModel file
            }
            boolean modified = tsFile.setLastModified(System.currentTimeMillis());
            logger.debug(tsFile.getAbsolutePath() +
                    " (" + Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(tsFile.lastModified())) + ") " +
                    modified);
        } catch (IOException e) {
            logger.warn("problem setting the external model checkout timestamp.");
        }
    }

    public File cacheFile(ExternalModel externalModel) throws IOException, ExternalModelException {
        Objects.requireNonNull(externalModel);
        File projectDataDir = project.getProjectDataDir();
        File file = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
        fileStorageService.createDirectory(file.getParentFile());
        ExternalModelCacheState state = getCacheState(externalModel);
        switch (state) {
            case NOT_CACHED:
            case CACHED_OUTDATED: {
                // TODO: handle file opened by other process
                if (file.canWrite() || !file.exists()) {
                    logger.debug("caching: " + file.getAbsolutePath());
                    if (externalModel.getAttachment() == null)
                        throw new ExternalModelException("external model has empty attachment");
                    Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
                    updateCheckoutTimestamp(externalModel);
                    File cacheFile = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
                    externalModelFileWatcher.add(externalModel, cacheFile);
                } else {
                    logger.error("file in local cache (" + file.getPath() + ") is not writable!");
                }
                break;
            }
            case CACHED_UP_TO_DATE:
            case CACHED_MODIFIED_AFTER_CHECKOUT:
                logger.debug("using cached file: " + file.getAbsolutePath());
                break;
            case CACHED_CONFLICTING_CHANGES:
                logger.warn("using cached file: " + file.getAbsolutePath() + ", file has conflicting changes!");
                break;
        }
        return file;
    }

    public void cleanupCache(SystemModel systemModel) {
        Set<String> toBeKept = new HashSet<>();
        File projectDataDir = project.getProjectDataDir();
        ExternalModelTreeIterator emi = new ExternalModelTreeIterator(systemModel);
        while (emi.hasNext()) {
            ExternalModel em = emi.next();
            ExternalModelCacheState cacheState = getCacheState(em);
            File emf = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, em);
            boolean needToKeep = false;
            //if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT ||
            //        cacheState == ExternalModelCacheState.CACHED_CONFLICTING_CHANGES) {
            needToKeep = true;
            toBeKept.add(emf.getAbsolutePath());
            File emc = getTimestampFile(emf);
            toBeKept.add(emc.getAbsolutePath());
            //}
            logger.info("file: '" + emf.getAbsolutePath() + "', [" + cacheState + "], needToKeep: " + needToKeep);
        }
        // go through cache directory, check if to keep, otherwise delete
        try {
            Files.walk(projectDataDir.toPath(), FileVisitOption.FOLLOW_LINKS).forEach(path -> {
                File file = path.toFile();
                if (file.isFile() && !toBeKept.contains(file.getAbsolutePath())) { // files not to be kept
                    logger.info("deleting: '" + file.getAbsolutePath() + "'");
                    file.delete();
                } else if (file.isDirectory() && file.list() != null && file.list().length == 0) { // empty directories
                    logger.info("deleting: '" + file.getAbsolutePath() + "'");
                    file.delete();
                } else {
                    logger.info("keeping: '" + file.getAbsolutePath() + "'");
                }
            });
        } catch (IOException e) {
            logger.error("error traversing project directory", e);
        }
    }

    public File forceCacheUpdate(ExternalModel externalModel) throws IOException {
        Objects.requireNonNull(externalModel);
        File projectDataDir = project.getProjectDataDir();
        File file = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
        fileStorageService.createDirectory(file.getParentFile());
        ExternalModelCacheState state = getCacheState(externalModel);
        switch (state) {
            case CACHED_MODIFIED_AFTER_CHECKOUT: // overwrite
            case CACHED_CONFLICTING_CHANGES:
                logger.warn("overwriting cached file: " + file.getAbsolutePath());
                Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
                this.updateCheckoutTimestamp(externalModel);
                break;
            default:
        }
        return file;
    }

    public InputStream getAttachmentAsStream(ExternalModel externalModel) throws IOException, ExternalModelException {
        switch (this.getCacheState(externalModel)) {
            case CACHED_UP_TO_DATE:
            case CACHED_MODIFIED_AFTER_CHECKOUT:
            case CACHED_CONFLICTING_CHANGES:
                File projectDataDir = project.getProjectDataDir();
                File cachedFile = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
                return new FileInputStream(cachedFile);
            case CACHED_OUTDATED:
                File writtenFile = cacheFile(externalModel);
                return new FileInputStream(writtenFile);
            default:
                return externalModel.getAttachmentAsStream();
        }
    }

    public void flushModifications(ExternalModel externalModel, SpreadsheetCellValueAccessor spreadsheetAccessor) throws ExternalModelException {
        if (spreadsheetAccessor != null) {
            if (spreadsheetAccessor.isModified()) {
                ExternalModelCacheState cacheState = this.getCacheState(externalModel);
                if (cacheState == ExternalModelCacheState.NOT_CACHED) {
                    logger.debug("Updating " + externalModel.getNodePath() + " with changes from parameters");
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(externalModel.getAttachment().length)) {
                        spreadsheetAccessor.saveChanges(bos);
                        externalModel.setAttachment(bos.toByteArray());
                    } catch (IOException e) {
                        logger.error("Error saving changes on spreadsheet to external model " + externalModel.getNodePath() + "(in memory).");
                        throw new ExternalModelException("error saving changes to external model" + externalModel.getNodePath());
                    }
                } else {
                    File projectDataDir = project.getProjectDataDir();
                    File file = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
                    externalModelFileWatcher.maskChangesTo(file);
                    logger.debug("Updating " + file.getAbsolutePath() + " with changes from parameters");
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        spreadsheetAccessor.saveChanges(fos);
                    } catch (FileNotFoundException e) {
                        logger.error("Error saving changes on spreadsheet to external model " + externalModel.getNodePath() + " (on cache file).");
                        throw new ExternalModelException("external model " + externalModel.getNodePath() + " is opened by other application");
                    } catch (IOException e) {
                        logger.error("Error saving changes on spreadsheet to external model " + externalModel.getNodePath() + " (on cache file).");
                        throw new ExternalModelException("error saving changes to external model " + externalModel.getNodePath());
                    } finally {
                        externalModelFileWatcher.unmaskChangesTo(file);
                    }
                }
            }
        }
    }

    private long getCheckoutTime(ExternalModel externalModel) {
        File projectDataDir = project.getProjectDataDir();
        File file = externalModelFileStorageService.createFilePathForExternalModel(projectDataDir, externalModel);
        return getCheckoutTime(file);
    }

}
