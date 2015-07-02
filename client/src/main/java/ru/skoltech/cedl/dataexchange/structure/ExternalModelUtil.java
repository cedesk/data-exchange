package ru.skoltech.cedl.dataexchange.structure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelUtil {

    public static ExternalModel fromFile(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        String fileName = file.getName();
        byte[] data = Files.readAllBytes(path);
        ExternalModel externalModel = new ExternalModel();
        externalModel.setName(fileName);
        externalModel.setAttachment(data);
        return externalModel;
    }

    public static File toFile(ExternalModel externalModel, File folder) {
        return null;
    }
}
