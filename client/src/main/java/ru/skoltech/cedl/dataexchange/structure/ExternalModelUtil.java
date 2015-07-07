package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.ProjectContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelUtil {

    public static ExternalModel fromFile(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        String fileName = file.getName();
        ExternalModel externalModel = new ExternalModel();
        externalModel.setName(fileName);
        externalModel.setAttachment(Files.readAllBytes(path));
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
        boolean newerOnDisk = file.lastModified() > externalModel.getLastModification(); // FIX: imprecise
        Files.write(file.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
        return file;
    }

    public static boolean isCached(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        File file = getExternalModelFile(externalModel);
        return file.exists() && file.canRead() && file.canWrite();

    }

    private static File getExternalModelFile(ExternalModel externalModel) {
        File folder = ProjectContext.getINSTANCE().getProjectDataDir(); // TODO: maybe include owningModel.NodePath
        return new File(folder, externalModel.getName());
    }
}
