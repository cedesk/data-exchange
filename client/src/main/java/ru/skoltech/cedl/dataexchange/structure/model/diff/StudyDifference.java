package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 12.05.2016.
 */
public class StudyDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(StudyDifference.class);

    protected Study study1;

    protected Study study2;

    private StudyDifference(Study study1, Study study2, String attribute,
                            ChangeType changeType, ChangeLocation changeLocation,
                            String value1, String value2) {
        this.study1 = study1;
        this.study2 = study2;
        this.attribute = attribute;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public PersistedEntity getChangedEntity() {
        if (changeType == ChangeType.MODIFY) {
            return changeLocation == ChangeLocation.ARG1 ? study1 : study2;
        } else {
            throw new IllegalArgumentException("Unknown change type and location combination");
        }
    }

    @Override
    public String getNodeName() {
        return study1.getName();
    }

    @Override
    public String getParameterName() {
        return "";
    }

    @Override
    public ModelNode getParentNode() {
        return study1.getSystemModel();
    }

    @Override
    public boolean isMergeable() {
        return false;
    }

    public static StudyDifference createStudyAttributesModified(Study study1, Study study2, String attribute,
                                                                String value1, String value2) {

        boolean n2newer = study2.getLatestModelModification() > study1.getLatestModelModification();
        ChangeLocation changeLocation = n2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new StudyDifference(study1, study2, attribute, ChangeType.MODIFY, changeLocation, value1, value2);
    }

    public static List<ModelDifference> computeDifferences(Study s1, Study s2, long latestStudy1Modification) {
        List<ModelDifference> modelDifferences = new LinkedList<>();

        long s1Version = s1.getVersion();
        long s2Version = s2.getVersion();
        if (s1Version != s2Version) {
            modelDifferences.add(createStudyAttributesModified(s1, s2, "version", Long.toString(s1Version), Long.toString(s2Version)));
        }

        UserRoleManagement urm1 = s1.getUserRoleManagement();
        UserRoleManagement urm2 = s2.getUserRoleManagement();
        if (urm1 != null && !urm1.equals(urm2)) {
            modelDifferences.add(createStudyAttributesModified(s1, s2, "userRoleManagement", "<>", "<>"));
        }

        StudySettings ss1 = s1.getStudySettings();
        StudySettings ss2 = s1.getStudySettings();
        if (urm1 != null & !ss1.equals(ss2)) {
            modelDifferences.add(createStudyAttributesModified(s1, s2, "studySettings", "<>", "<>"));
        }

        SystemModel m1 = s1.getSystemModel();
        SystemModel m2 = s2.getSystemModel();
        if (m1 != null && m2 != null) {
            modelDifferences.addAll(NodeDifference.computeDifferences(m1, m2, latestStudy1Modification));
        }
        return modelDifferences;
    }

    @Override
    public void mergeDifference() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StudyDifference{");
        sb.append("attribute='").append(attribute).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", changeLocation=").append(changeLocation);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append(", study1=").append(study1);
        sb.append(", study2=").append(study2);
        sb.append('}');
        return sb.toString();
    }
}
