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

package ru.skoltech.cedl.dataexchange.structure;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.PersistedEntity;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.repository.jpa.RevisionEntityRepository;
import ru.skoltech.cedl.dataexchange.service.NodeDifferenceService;
import ru.skoltech.cedl.dataexchange.service.StudyService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for merging differences of projects.
 * <p>
 * Created by Nikolay Groshkov on 07-Jul-17.
 */
public class DifferenceMergeHandler {

    private static final Logger logger = Logger.getLogger(DifferenceMergeHandler.class);

    private ModelUpdateHandler modelUpdateHandler;
    private ExternalModelFileHandler externalModelFileHandler;
    private StudyService studyService;
    private NodeDifferenceService nodeDifferenceService;
    private final RevisionEntityRepository revisionEntityRepository;

    @Autowired
    public DifferenceMergeHandler(RevisionEntityRepository revisionEntityRepository) {
        this.revisionEntityRepository = revisionEntityRepository;
    }

    public void setModelUpdateHandler(ModelUpdateHandler modelUpdateHandler) {
        this.modelUpdateHandler = modelUpdateHandler;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setNodeDifferenceService(NodeDifferenceService nodeDifferenceService) {
        this.nodeDifferenceService = nodeDifferenceService;
    }

    /**
     * TODO add javadoc
     *
     * @param localStudy
     * @param remoteStudy
     * @return
     */
    public List<ModelDifference> computeStudyDifferences(Study localStudy, Study remoteStudy) {
        List<ModelDifference> modelDifferences = new LinkedList<>();

        // attributes
        List<AttributeDifference> attributeDifferences = getAttributeDifferences(localStudy, remoteStudy);
        if (!attributeDifferences.isEmpty()) {
            modelDifferences.add(createStudyAttributesModified(localStudy, remoteStudy, attributeDifferences));
        }
        // system model
        SystemModel localSystemModel = localStudy.getSystemModel();
        SystemModel remoteSystemModel2 = remoteStudy.getSystemModel();
        if (localSystemModel != null && remoteSystemModel2 != null) {
            modelDifferences.addAll(nodeDifferenceService.computeNodeDifferences(localSystemModel, remoteSystemModel2, localStudy.getRevision()));
        }

        return modelDifferences.stream()
                .peek(modelDifference -> modelDifference.setAuthor(this.retrieveAuthor(modelDifference)))
                .collect(Collectors.toList());
    }

    /**
     * Compacting all attributes differences into one study modification
     * TODO add javadoc
     *
     * @param modelDifference
     * @return
     */
    private String retrieveAuthor(ModelDifference modelDifference) {
        if (modelDifference.isMergeable()) {
            PersistedEntity persistedEntity = modelDifference.getChangedEntity();
            long id = persistedEntity.getId();
            Class<? extends PersistedEntity> clazz = persistedEntity.getClass();
            CustomRevisionEntity revisionEntity;
            try {
                revisionEntity = revisionEntityRepository.lastRevisionEntity(id, clazz);
            } catch (Exception e) {
                logger.debug("Loading revision history failed: " +
                        persistedEntity.getClass().getSimpleName() + "[" + persistedEntity.getId() + "]");
                revisionEntity = null;
            }
            return revisionEntity != null ? revisionEntity.getUsername() : "<none>";
        } else if (modelDifference.isRevertible()) {
            return "<you>";
        } else {
            return null;
        }
    }

    /**
     * Compacting all attributes differences into one study modification
     * TODO add javadoc
     *
     * @param study1
     * @param study2
     * @param differences
     * @return
     */
    public ModelDifference createStudyAttributesModified(Study study1, Study study2, List<AttributeDifference> differences) {
        StringBuilder sbAttributes = new StringBuilder(), sbValues1 = new StringBuilder(), sbValues2 = new StringBuilder();
        for (AttributeDifference diff : differences) {
            if (sbAttributes.length() > 0) {
                sbAttributes.append('\n');
                sbValues1.append('\n');
                sbValues2.append('\n');
            }
            sbAttributes.append(diff.attributeName);
            sbValues1.append(diff.value1);
            sbValues2.append(diff.value2);
        }
        boolean s2newer = study1.getRevision() < study2.getRevision();
        ModelDifference.ChangeLocation changeLocation = s2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new StudyDifference(study1, study2, ModelDifference.ChangeType.MODIFY, changeLocation, sbAttributes.toString(), sbValues1.toString(), sbValues2.toString(), studyService);
    }

    /**
     * TODO add javadoc
     *
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    public List<ModelDifference> mergeChangesOntoFirst(List<ModelDifference> modelDifferences) throws MergeException {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (modelDifference.isMergeable()) {
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
     * TODO add javadoc
     *
     * @param modelDifference
     * @return
     * @throws MergeException
     */
    public boolean mergeOne(ModelDifference modelDifference) throws MergeException {
        logger.debug("merging " + modelDifference.getElementPath());
        modelDifference.mergeDifference();
        if (modelDifference instanceof ParameterDifference) {
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
        } else if (modelDifference instanceof ExternalModelDifference) {
            ExternalModelDifference emd = (ExternalModelDifference) modelDifference;
            ExternalModel externalModel = emd.getExternalModel1();
            return updateCacheAndParameters(externalModel);
        }
        return true;
    }

    /**
     * TODO add javadoc
     *
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    public List<ModelDifference> revertChangesOnFirst(List<ModelDifference> modelDifferences) throws MergeException {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (modelDifference.isRevertible()) {
                boolean success = revertOne(modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        modelDifferences.removeAll(appliedDifferences);
        return appliedDifferences;
    }

    /**
     * TODO add javadoc
     *
     * @param modelDifference
     * @return
     * @throws MergeException
     */
    public boolean revertOne(ModelDifference modelDifference) throws MergeException {
        logger.debug("reverting " + modelDifference.getElementPath());
        modelDifference.revertDifference();
        if (modelDifference instanceof ParameterDifference) {
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
        } else if (modelDifference instanceof ExternalModelDifference) {
            ExternalModelDifference emd = (ExternalModelDifference) modelDifference;
            ExternalModel externalModel = emd.getExternalModel1();
            return updateCacheAndParameters(externalModel);
        }
        return true;
    }

    private boolean updateCacheAndParameters(ExternalModel externalModel) throws MergeException {
        try {
            // update cached file
            externalModelFileHandler.forceCacheUpdate(externalModel);
            // update parameters from new file
            modelUpdateHandler.applyParameterChangesFromExternalModel(externalModel, null, null);
        } catch (ExternalModelException e) {
            logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
        } catch (IOException e) {
            logger.error("failed to update cached external model: " + externalModel.getNodePath(), e);
            throw new MergeException("failed to updated cached external model: " + externalModel.getName());
        }
        return true;
    }

    private static List<AttributeDifference> getAttributeDifferences(Study study1, Study study2) {
        List<AttributeDifference> differences = new LinkedList<>();
        if (study1.getVersion() != study2.getVersion()) {
            differences.add(new AttributeDifference("version", study1.getVersion(), study2.getVersion()));
        }
        if ((study1.getUserRoleManagement() == null && study2.getUserRoleManagement() != null) || (study1.getUserRoleManagement() != null && study2.getUserRoleManagement() == null)
                || (study1.getUserRoleManagement() != null && !study1.getUserRoleManagement().equals(study2.getUserRoleManagement()))) {
            UserRoleManagement urm1 = study1.getUserRoleManagement();
            UserRoleManagement urm2 = study1.getUserRoleManagement();
            String urmHash1 = urm1 != null ? String.valueOf(urm1.hashCode()) : null;
            String urmHash2 = urm2 != null ? String.valueOf(urm2.hashCode()) : null;
            differences.add(new AttributeDifference("userRoleManagement", urmHash1, urmHash2));
        }
        if ((study1.getStudySettings() == null && study2.getStudySettings() != null) || (study1.getStudySettings() != null && study2.getStudySettings() == null)
                || (study1.getStudySettings() != null && !study1.getStudySettings().equals(study2.getStudySettings()))) {
            StudySettings studySettings1 = study1.getStudySettings();
            StudySettings studySettings2 = study2.getStudySettings();
            String sync1 = studySettings1 != null ? "isSyncEnabled=" + String.valueOf(studySettings1.getSyncEnabled()) : null;
            String sync2 = studySettings2 != null ? "isSyncEnabled=" + String.valueOf(studySettings2.getSyncEnabled()) : null;
            differences.add(new AttributeDifference("studySettings", sync1, sync2));
        }
        return differences;
    }

}
