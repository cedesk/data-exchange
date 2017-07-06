package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ModelUpdateUtil;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by d.knoll on 24/05/2017.
 */
public class DifferenceMerger {

    private static final Logger logger = Logger.getLogger(DifferenceMerger.class);

    public static boolean mergeOne(Project project, ModelDifference modelDifference) throws MergeException {
        logger.debug("merging " + modelDifference.getElementPath());
        modelDifference.mergeDifference();
        if (modelDifference instanceof ParameterDifference) {
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
        } else if (modelDifference instanceof ExternalModelDifference) {
            ExternalModelDifference emd = (ExternalModelDifference) modelDifference;
            ExternalModel externalModel = emd.getExternalModel1();
            return updateCacheAndParameters(project, externalModel);
        }
        return true;
    }

    private static boolean updateCacheAndParameters(Project project, ExternalModel externalModel) {
        try {
            // update cached file
            ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
            externalModelFileHandler.forceCacheUpdate(project, externalModel);
            // update parameters from new file
            ModelUpdateUtil.applyParameterChangesFromExternalModel(project, externalModel, externalModelFileHandler, null, null);
        } catch (ExternalModelException e) {
            logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
        } catch (IOException e) {
            logger.error("failed to update cached external model: " + externalModel.getNodePath(), e);
            StatusLogger.getInstance().log("failed to updated cached external model: " + externalModel.getName(), true);
            return false;
        }
        return true;
    }

    public static boolean revertOne(Project project, ModelDifference modelDifference) throws MergeException {
        logger.debug("reverting " + modelDifference.getElementPath());
        modelDifference.revertDifference();
        if (modelDifference instanceof ParameterDifference) {
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
        } else if (modelDifference instanceof ExternalModelDifference) {
            ExternalModelDifference emd = (ExternalModelDifference) modelDifference;
            ExternalModel externalModel = emd.getExternalModel1();
            return updateCacheAndParameters(project, externalModel);
        }
        return true;
    }

    /**
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    public static List<ModelDifference> mergeChangesOntoFirst(Project project, List<ModelDifference> modelDifferences) throws MergeException {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (modelDifference.isMergeable()) {
                boolean success = mergeOne(project, modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        modelDifferences.removeAll(appliedDifferences);
        return appliedDifferences;
    }

    /**
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    public static List<ModelDifference> revertChangesOnFirst(Project project, List<ModelDifference> modelDifferences) throws MergeException {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (modelDifference.isRevertible()) {
                boolean success = revertOne(project, modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        modelDifferences.removeAll(appliedDifferences);
        return appliedDifferences;
    }
}
