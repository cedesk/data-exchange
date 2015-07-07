package ru.skoltech.cedl.dataexchange.structure;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelUtil {

    private static Logger logger = Logger.getLogger(ExternalModelUtil.class);

    public static ExternalModel fromFile(File file, ModelNode parent) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        String fileName = file.getName();
        ExternalModel externalModel = new ExternalModel();
        externalModel.setName(fileName);
        externalModel.setAttachment(Files.readAllBytes(path));
        externalModel.setParent(parent);
        // TODO: must store to DB
        return externalModel;
    }

    public static File toFile(ExternalModel externalModel, File folder) throws IOException {
        Objects.requireNonNull(externalModel);
        Objects.requireNonNull(folder);
        File file = new File(folder, externalModel.getName());
        Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
        return file;
    }

    public static File cacheFile(ExternalModel externalModel) throws IOException {
        Objects.requireNonNull(externalModel);
        File file = getExternalModelFile(externalModel);
        if (file.exists()) {
            boolean newerInRepository = file.lastModified() < externalModel.getLastModification(); // FIX: imprecise
            if (newerInRepository) {
                if (file.canWrite()) {
                    Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
                } else {
                    logger.error("file in local cache (" + file.getPath() + ") is not writable!");
                }
            } else {
                logger.error("file in local cache (" + file.getPath() + ") is newer than in the repository!");
            }
        } else {
            if (file.canWrite()) {
                Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
            } else {
                logger.error("file in local cache (" + file.getPath() + ") is not writable!");
            }
        }
        return file;
    }

    public static boolean isCached(ExternalModel externalModel, boolean readOnly) {
        Objects.requireNonNull(externalModel);
        File file = getExternalModelFile(externalModel);
        return file.exists() && file.canRead() && (readOnly || file.canWrite());
    }

    private static File getExternalModelFile(ExternalModel externalModel) {
        String nodePath = makePath(externalModel);
        File projectDataDir = ProjectContext.getINSTANCE().getProjectDataDir(); // TODO: maybe include owningModel.NodePath
        File nodeDir = new File(projectDataDir, nodePath);
        return new File(nodeDir, externalModel.getName());
    }

    private static String makePath(ExternalModel externalModel) {
        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.pathSeparator);
        return path;
    }
}
