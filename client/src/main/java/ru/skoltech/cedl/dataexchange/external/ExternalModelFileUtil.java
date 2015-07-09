package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelFileUtil {

    private static Logger logger = Logger.getLogger(ExternalModelFileUtil.class);

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
                } else {
                    logger.error("file in local cache (" + file.getPath() + ") is not writable!");
                }
                break;
            }
            case CACHED_UP_TO_DATE:
                logger.debug("using cached file: " + file.getAbsolutePath());
        }
        return file;
    }

    public static ExternalModelCacheState getCacheState(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);
        File file = getFilePathInCache(externalModel);
        if (file.exists() && externalModel.getLastModification() != null) {
            boolean newerInRepository = file.lastModified() < externalModel.getLastModification(); // FIX: imprecise
            if (newerInRepository) {
                return ExternalModelCacheState.CACHED_OUTDATED;
            } else {
                return ExternalModelCacheState.CACHED_UP_TO_DATE;
            }
        }
        return ExternalModelCacheState.NOT_CACHED;
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
        File projectDataDir = ProjectContext.getINSTANCE().getProjectDataDir();
        File nodeDir = new File(projectDataDir, nodePath);
        String rectifiedFileName = externalModel.getName().replace(' ', '_');
        return new File(nodeDir, rectifiedFileName);
    }

    private static String makePath(ExternalModel externalModel) {
        String path = externalModel.getParent().getNodePath();
        path = path.replace(' ', '_');
        path = path.replace(ModelNode.NODE_SEPARATOR, File.separator);
        return path;
    }

    public static void openOnDesktop(ExternalModel externalModel) {
        File spreadsheetFile = null;
        try {
            spreadsheetFile = ExternalModelFileUtil.cacheFile(externalModel);
        } catch (IOException ioe) {
            logger.error("Error saving external model to spreadsheet.", ioe);
            return;
        }
        if (spreadsheetFile != null) {
            try {
                Desktop.getDesktop().edit(spreadsheetFile);
            } catch (Exception e) {
                logger.error("Error opening spreadsheet with default editor.", e);
            }
        }
    }

    public static List<ParameterUpdate> retrieveParameterUpdates(ExternalModel externalModel) {
        ModelNode modelNode = externalModel.getParent();
        List<ParameterUpdate> updates = new LinkedList<>();
        ExternalModelEvaluator evaluator = ExternalModelEvaluatorFactory.getEvaluator(externalModel);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter references external model
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                if (parameterModel.getValueReference() != null && !parameterModel.getValueReference().isEmpty()) {
                    String[] components = parameterModel.getValueReference().split(":");
                    if (externalModel.getName().equals(components[1])) {
                        try {
                            Double value = evaluator.getValue(components[2]);
                            //TODO: if(parameterModel.getValue() notEqual value)
                            ParameterUpdate parameterUpdate = new ParameterUpdate(parameterModel, value);
                            updates.add(parameterUpdate);
                        } catch (ExternalModelException e) {
                            logger.error("unable to evaluate from: " + parameterModel.getValueReference());
                        }
                    }
                } else {
                    logger.warn("parameter " + modelNode.getNodePath() + "\\" + parameterModel.getName() + " has empty valueReference");
                }
            }
        }
        return updates;
    }
}
