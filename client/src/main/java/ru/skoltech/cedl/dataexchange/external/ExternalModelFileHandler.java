package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelFileHandler {

    private static Logger logger = Logger.getLogger(ExternalModelFileHandler.class);

    private Set<ExternalModel> changedExternalModels = new HashSet<>();

    private Project project;

    public ExternalModelFileHandler(Project project) {
        this.project = project;
    }

    public static ExternalModel newFromFile(File file, ModelNode parent) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        String fileName = file.getName();
        ExternalModel externalModel = new ExternalModel();
        externalModel.setName(fileName);
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setParent(parent);
        return externalModel;
    }

    public static ExternalModel readAttachmentFromFile(ExternalModel externalModel) throws IOException {
        File file = getFilePathInCache(externalModel);
        Path path = Paths.get(file.getAbsolutePath());
        externalModel.setAttachment(Files.readAllBytes(path));
        return externalModel;
    }

    public static ExternalModel readAttachmentFromFile(ExternalModel externalModel, File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        externalModel.setAttachment(Files.readAllBytes(path));
        return externalModel;
    }

    public static File toFile(ExternalModel externalModel, File folder) throws IOException {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(folder);
        File file = new File(folder, externalModel.getName());
        Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
        return file;
    }

    public static ExternalModelCacheState getCacheState(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        ExternalModelCacheState cacheState = ExternalModelCacheState.NOT_CACHED;
        File file = getFilePathInCache(externalModel);
        Long modelLastStored = externalModel.getLastModification();
        if (file.exists() && modelLastStored != null) {
            long checkoutTime = getCheckoutTime(file);
            long fileLastModified = file.lastModified();
            boolean newerInRepository = modelLastStored > checkoutTime;
            boolean locallyModified = checkoutTime < fileLastModified;
            if (newerInRepository) {
                if (locallyModified) {
                    cacheState = ExternalModelCacheState.CACHED_CONFLICTING_CHANGES;
                } else {
                    cacheState = ExternalModelCacheState.CACHED_OUTDATED;
                }
            } else {
                if (locallyModified) {
                    cacheState = ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT;
                } else {
                    cacheState = ExternalModelCacheState.CACHED_UP_TO_DATE;
                }
            }
        }
        return cacheState;
    }

    public static long getCheckoutTime(File file) {
        File tsFile = getTimestampFile(file);
        if (!tsFile.exists()) {
            logger.error("external model is missing checkout timestamp");
        }
        return tsFile.lastModified();
    }

    private static File getTimestampFile(File file) {
        return new File(file.getAbsolutePath() + ".tstamp");
    }

    /**
     * This method only forms the full path where the external model would be cached.<br/>
     * It does not actually assure the file nor the folder exist.
     *
     * @param externalModel
     * @return a file of the location where the external model would be stored.
     */
    public static File getFilePathInCache(ExternalModel externalModel) {
        String nodePath = makePath(externalModel);
        File projectDataDir = ProjectContext.getInstance().getProjectDataDir();
        File nodeDir = new File(projectDataDir, nodePath);
        String rectifiedFileName = externalModel.getName().replace(' ', '_');
        return new File(nodeDir, rectifiedFileName);
    }

    public static String makePath(ExternalModel externalModel) {
        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.separator);
        return path;
    }

    public static void updateCheckoutTimestamp(ExternalModel externalModel) {
        File file = getFilePathInCache(externalModel);
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

    public static long getCheckoutTime(ExternalModel externalModel) {
        return getCheckoutTime(getFilePathInCache(externalModel));
    }

    public File cacheFile(ExternalModel externalModel) throws IOException {
        Objects.requireNonNull(externalModel);
        File file = getFilePathInCache(externalModel);
        StorageUtils.makeDirectory(file.getParentFile());
        ExternalModelCacheState state = getCacheState(externalModel);
        switch (state) {
            case NOT_CACHED:
            case CACHED_OUTDATED: {
                // TODO: handle file opened by other process
                if (file.canWrite() || !file.exists()) {
                    logger.debug("caching: " + file.getAbsolutePath());
                    Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
                    updateCheckoutTimestamp(externalModel);
                    project.addExternalModelFileWatcher(externalModel);
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

    public File forceCacheUpdate(ExternalModel externalModel) throws IOException {
        Objects.requireNonNull(externalModel);
        File file = getFilePathInCache(externalModel);
        StorageUtils.makeDirectory(file.getParentFile());
        ExternalModelCacheState state = getCacheState(externalModel);
        switch (state) {
            case CACHED_MODIFIED_AFTER_CHECKOUT: // overwrite
            case CACHED_CONFLICTING_CHANGES:
                logger.warn("overwriting cached file: " + file.getAbsolutePath());
                toFile(externalModel, file.getParentFile());
                updateCheckoutTimestamp(externalModel);
                break;
            default:
        }
        return file;
    }

    public InputStream getAttachmentAsStream(ExternalModel externalModel) throws IOException {
        switch (ExternalModelFileHandler.getCacheState(externalModel)) {
            case CACHED_UP_TO_DATE:
            case CACHED_MODIFIED_AFTER_CHECKOUT:
            case CACHED_CONFLICTING_CHANGES:
                File cachedFile = getFilePathInCache(externalModel);
                return new FileInputStream(cachedFile);
            case CACHED_OUTDATED:
                File writtenFile = cacheFile(externalModel);
                return new FileInputStream(writtenFile);
            default:
                return new ByteArrayInputStream(externalModel.getAttachment());
        }
    }

    public Set<ExternalModel> getChangedExternalModels() {
        return changedExternalModels;
    }

    public void addChangedExternalModel(ExternalModel externalModel) {
        changedExternalModels.add(externalModel);
    }

    public void openOnDesktop(ExternalModel externalModel) {
        File spreadsheetFile = null;
        try {
            spreadsheetFile = cacheFile(externalModel);
        } catch (IOException ioe) {
            logger.error("Error saving external model to spreadsheet.", ioe);
            return;
        }
        if (spreadsheetFile != null) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.EDIT)) {
                    desktop.edit(spreadsheetFile);
                } else {
                    StatusLogger.getInstance().log("Unable to open file!", true);
                }
            } catch (Exception e) {
                logger.error("Error opening spreadsheet with default editor.", e);
            }
        }
    }
}
