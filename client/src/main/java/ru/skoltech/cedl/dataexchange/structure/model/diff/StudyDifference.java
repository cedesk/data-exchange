package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static ru.skoltech.cedl.dataexchange.Utils.TIME_AND_DATE_FOR_USER_INTERFACE;

/**
 * Created by D.Knoll on 12.05.2016.
 */
public class StudyDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(StudyDifference.class);

    protected Study study1;

    protected Study study2;

    private StudyDifference(Study study1, Study study2, ChangeType changeType, ChangeLocation changeLocation,
                            String attribute, String value1, String value2) {
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
        return changeType == ChangeType.MODIFY && changeLocation == ChangeLocation.ARG2;
    }

    @Override
    public boolean isRevertible() {
        //TODO implement
        return false;
    }

    public static List<ModelDifference> computeDifferences(Study s1, Study s2, long latestStudy1Modification) {
        List<ModelDifference> modelDifferences = new LinkedList<>();

        // attributes
        List<AttributeDifference> attributeDifferences = getAttributeDifferences(s1, s2);
        if (!attributeDifferences.isEmpty()) {
            modelDifferences.add(createStudyAttributesModified(s1, s2, attributeDifferences));
        }
        // system model
        SystemModel systemModel1 = s1.getSystemModel();
        SystemModel sSystemModel2 = s2.getSystemModel();
        if (systemModel1 != null && sSystemModel2 != null) {
            modelDifferences.addAll(NodeDifference.computeDifferences(systemModel1, sSystemModel2, latestStudy1Modification));
        }
        return modelDifferences;
    }

    /**
     * compacting all attributes differences into one study modification
     */
    public static ModelDifference createStudyAttributesModified(Study study1, Study study2, List<AttributeDifference> differences) {
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
        boolean p2newer = isNewer(study1, study2);
        ChangeLocation changeLocation = p2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new StudyDifference(study1, study2, ChangeType.MODIFY, changeLocation, sbAttributes.toString(), sbValues1.toString(), sbValues2.toString());
    }

    /**
     * @return true if s2 is newer than s1
     */
    private static boolean isNewer(Study s1, Study s2) {
        Long mod1 = s1.getLatestModelModification();
        Long mod2 = s2.getLatestModelModification();
        mod1 = mod1 != null ? mod1 : 0L;
        mod2 = mod2 != null ? mod2 : 0L;
        return mod1 < mod2;
    }

    private static List<AttributeDifference> getAttributeDifferences(Study study1, Study study2) {
        List<AttributeDifference> differences = new LinkedList<>();
  /*      if ((study1.getLatestModelModification() == null && study2.getLatestModelModification() != null) || (study1.getLatestModelModification() != null && study2.getLatestModelModification() == null)
                || (study1.getLatestModelModification() != null && !study1.getLatestModelModification().equals(study2.getLatestModelModification()))) {
            differences.add(new AttributeDifference("latestModelModification",
                    toTime(study1.getLatestModelModification()), toTime(study2.getLatestModelModification())));
        } */
        if (study1.getVersion() != study2.getVersion()) {
            differences.add(new AttributeDifference("version", study1.getVersion(), study2.getVersion()));
        }
        if ((study1.getUserRoleManagement() == null && study2.getUserRoleManagement() != null) || (study1.getUserRoleManagement() != null && study2.getUserRoleManagement() == null)
                || (study1.getUserRoleManagement() != null && !study1.getUserRoleManagement().equals(study2.getUserRoleManagement()))) {
            differences.add(new AttributeDifference("userRoleManagement", toHash(study1.getUserRoleManagement()), toHash(study2.getUserRoleManagement())));
        }
        if ((study1.getStudySettings() == null && study2.getStudySettings() != null) || (study1.getStudySettings() != null && study2.getStudySettings() == null)
                || (study1.getStudySettings() != null && !study1.getStudySettings().equals(study2.getStudySettings()))) {
            differences.add(new AttributeDifference("studySettings", extractSync(study1.getStudySettings()), extractSync(study2.getStudySettings())));
        }
        return differences;
    }

    private static String extractSync(StudySettings studySettings) {
        return studySettings != null ? "isSyncEnabled=" + String.valueOf(studySettings.getSyncEnabled()) : null;
    }

    private static String toHash(UserRoleManagement userRoleManagement) {
        return userRoleManagement != null ? String.valueOf(userRoleManagement.hashCode()) : null;
    }

    private static String toTime(Long timestamp) {
        return timestamp != null ? TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(timestamp)) : null;
    }

    @Override
    public void mergeDifference() {
        if (changeType == ChangeType.MODIFY && changeLocation == ChangeLocation.ARG2) {
            study1.setVersion(study2.getVersion());
            study1.setUserRoleManagement(study2.getUserRoleManagement());
            study1.setStudySettings(study2.getStudySettings());
        } else {
            logger.error("MERGE IMPOSSIBLE:\n" + toString());
        }
    }

    @Override
    public void revertDifference() {
        throw new NotImplementedException();
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
