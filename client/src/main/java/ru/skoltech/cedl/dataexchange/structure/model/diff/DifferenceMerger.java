package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by d.knoll on 24/05/2017.
 */
public class DifferenceMerger {

    private static final Logger logger = Logger.getLogger(DifferenceMerger.class);

    public static boolean mergeOne(ModelDifference modelDifference) {
        logger.debug("merging " + modelDifference.getNodeName() + "::" + modelDifference.getParameterName());
        modelDifference.mergeDifference();
        if (modelDifference instanceof ParameterDifference) {
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
            // TODO: update sinks
            //ParameterLinkRegistry parameterLinkRegistry = ProjectContext.getInstance().getProject().getParameterLinkRegistry();
            //parameterLinkRegistry.updateSinks(parameterDifference.getParameter());
        }
        return true;
    }

    /**
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    public static List<ModelDifference> applyChangesOnSecondToFirst(List<ModelDifference> modelDifferences) {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (modelDifference.hasChangeOnSecond()) {
                boolean success = mergeOne(modelDifference);
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
    public static List<ModelDifference> revertChangesOnFirst(List<ModelDifference> modelDifferences) {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (modelDifference.hasChangeOnFirst()) {
                boolean success = mergeOne(modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        modelDifferences.removeAll(appliedDifferences);
        return appliedDifferences;
    }
}
