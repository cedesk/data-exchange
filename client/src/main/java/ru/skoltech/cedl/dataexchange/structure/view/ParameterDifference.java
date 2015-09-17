package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class ParameterDifference extends ModelDifference {

    protected ParameterModel parameter;

    public ParameterDifference(ParameterModel parameter, String attribute, ChangeType changeType, String value1, String value2) {
        this.parameter = parameter;
        this.attribute = attribute;
        this.changeType = changeType;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public String getNodeName() {
        return parameter.getNodePath();
    }

    @Override
    public String getNodePath() {
        return parameter.getNodePath();
    }

    public ParameterModel getParameter() {
        return parameter;
    }

    public void setParameter(ParameterModel parameter) {
        this.parameter = parameter;
    }

    public void mergeDifference() {
        switch (changeType) {
            case MODIFY_PARAMETER:
                break;
            default:
                throw new IllegalArgumentException("NO ACTION CHANGE TYPE " + changeType.toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterDifference{");
        sb.append("parameter='").append(parameter.getName()).append('\'');
        sb.append(", attribute='").append(attribute).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
