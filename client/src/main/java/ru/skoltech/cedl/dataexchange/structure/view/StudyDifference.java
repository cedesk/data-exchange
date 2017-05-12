package ru.skoltech.cedl.dataexchange.structure.view;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.Study;

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

    public static StudyDifference createStudyAttributesModified(Study study1, Study study2, String attribute,
                                                                String value1, String value2) {

        boolean n2newer = study2.getLatestModelModification() > study1.getLatestModelModification();
        ChangeLocation changeLocation = n2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new StudyDifference(study1, study2, attribute, ChangeType.CHANGE_STUDY, changeLocation, value1, value2);
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
    public boolean isMergeable() {
        return false;
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
