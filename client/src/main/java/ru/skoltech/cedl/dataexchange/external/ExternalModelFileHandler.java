package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModelTreeIterator;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.*;
import java.nio.file.*;
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

    public Set<ExternalModel> getChangedExternalModels() {
        return changedExternalModels;
    }

    public static ExternalModel newFromFile(File file, ModelNode parent) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        String fileName = file.getName();
        ExternalModel externalModel = new ExternalModel();
        externalModel.setName(fileName);
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setLastModification(file.lastModified());
        externalModel.setParent(parent);
        return externalModel;
    }

    public static ExternalModel readAttachmentFromFile(Project project, ExternalModel externalModel) throws IOException {
        File file = getFilePathInCache(project, externalModel);
        Path path = Paths.get(file.getAbsolutePath());
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setLastModification(file.lastModified());
        return externalModel;
    }

    public static ExternalModel readAttachmentFromFile(ExternalModel externalModel, File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setLastModification(file.lastModified());
        return externalModel;
    }

    public static File toFile(ExternalModel externalModel, File folder) throws IOException {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(folder);
        File file = new File(folder, externalModel.getName());
        Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
        return file;
    }

    public static ExternalModelCacheState getCacheState(Project project, ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        ExternalModelCacheState cacheState = ExternalModelCacheState.NOT_CACHED;
        File modelFile = getFilePathInCache(project, externalModel);
        Long modelLastStored = externalModel.getLastModification();
        if (modelFile.exists() && modelLastStored != null) {
            long checkoutTime = getCheckoutTime(modelFile);
            long fileLastModified = modelFile.lastModified();
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
    public static File getFilePathInCache(Project project, ExternalModel externalModel) {
        String nodePath = makePath(externalModel);
        File projectDataDir = project.getProjectDataDir();
        File nodeDir = new File(projectDataDir, nodePath);
        String rectifiedFileName = externalModel.getId() + "_" + externalModel.getName().replace(' ', '_');
        return new File(nodeDir, rectifiedFileName);
    }

    public static String makePath(ExternalModel externalModel) {
        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.separator);
        return path;
    }

    public static void updateCheckoutTimestamp(Project project, ExternalModel externalModel) {
        File file = getFilePathInCache(project, externalModel);
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

    public static long getCheckoutTime(Project project, ExternalModel externalModel) {
        return getCheckoutTime(getFilePathInCache(project, externalModel));
    }

    public void addChangedExternalModel(ExternalModel externalModel) {
        changedExternalModels.add(externalModel);
    }

    public File cacheFile(Project project, ExternalModel externalModel) throws IOException, ExternalModelException {
        Objects.requireNonNull(externalModel);
        File file = getFilePathInCache(project, externalModel);
        StorageUtils.makeDirectory(file.getParentFile());
        ExternalModelCacheState state = getCacheState(project, externalModel);
        switch (state) {
            case NOT_CACHED:
            case CACHED_OUTDATED: {
                // TODO: handle file opened by other process
                if (file.canWrite() || !file.exists()) {
                    logger.debug("caching: " + file.getAbsolutePath());
                    if (externalModel.getAttachment() == null)
                        throw new ExternalModelException("external model has empty attachment");
                    Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
                    updateCheckoutTimestamp(project, externalModel);
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

    public void cleanupCache() {
        Set<String> toBeKept = new HashSet<>();
        SystemModel systemModel = project.getStudy().getSystemModel();
        ExternalModelTreeIterator emi = new ExternalModelTreeIterator(systemModel);
        while (emi.hasNext()) {
            ExternalModel em = emi.next();
            ExternalModelCacheState cacheState = getCacheState(project, em);
            File emf = getFilePathInCache(project, em);
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
        File projectDataDir = project.getProjectDataDir();
        try {
            Files.walk(projectDataDir.toPath(), FileVisitOption.FOLLOW_LINKS).forEach(path -> {
                File file = path.toFile();
                if (file.isFile() && !toBeKept.contains(file.getAbsolutePath())) { // files not to be kept
                    logger.info("deleting: '" + file.getAbsolutePath() + "'");
                    file.delete();
                } else if (file.isDirectory() && file.list() != null&& file.list().length == 0) { // empty directories
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
        File file = getFilePathInCache(project, externalModel);
        StorageUtils.makeDirectory(file.getParentFile());
        ExternalModelCacheState state = getCacheState(project, externalModel);
        switch (state) {
            case CACHED_MODIFIED_AFTER_CHECKOUT: // overwrite
            case CACHED_CONFLICTING_CHANGES:
                logger.warn("overwriting cached file: " + file.getAbsolutePath());
                Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
                updateCheckoutTimestamp(project, externalModel);
                break;
            default:
        }
        return file;
    }

    public InputStream getAttachmentAsStream(ExternalModel externalModel) throws IOException, ExternalModelException {
        switch (ExternalModelFileHandler.getCacheState(project, externalModel)) {
            case CACHED_UP_TO_DATE:
            case CACHED_MODIFIED_AFTER_CHECKOUT:
            case CACHED_CONFLICTING_CHANGES:
                File cachedFile = getFilePathInCache(project, externalModel);
                return new FileInputStream(cachedFile);
            case CACHED_OUTDATED:
                File writtenFile = cacheFile(project, externalModel);
                return new FileInputStream(writtenFile);
            default:
                if (externalModel.getAttachment() == null)
                    throw new ExternalModelException("external model has empty attachment");
                return new ByteArrayInputStream(externalModel.getAttachment());
        }
    }

}
