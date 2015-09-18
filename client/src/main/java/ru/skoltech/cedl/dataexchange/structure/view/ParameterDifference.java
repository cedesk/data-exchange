package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class ParameterDifference extends ModelDifference {

    private final ParameterModel parameter1;

    private ParameterModel parameter2;

    private ParameterDifference(ParameterModel parameter1, ParameterModel parameter2, ChangeType changeType,
                                String attributes, String values1, String values2) {
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        this.changeType = changeType;
        this.attribute = attributes;
        this.value1 = values1;
        this.value2 = values2;
    }

    public ParameterDifference(ParameterModel parameter1, ChangeType changeType, String value1, String value2) {
        this.parameter1 = parameter1;
        this.changeType = changeType;
        this.value1 = value1;
        this.value2 = value2;
    }

    public static ParameterDifference createModifiedParameterAttributes(ParameterModel parameter1, ParameterModel parameter2,
                                                                        String attributes, String values1, String values2) {
        return new ParameterDifference(parameter1, parameter2, ChangeType.MODIFY_PARAMETER, attributes, values1, values2);
    }

    public static ModelDifference createRemovedParameter(ParameterModel p1, String name) {
        return new ParameterDifference(p1, ChangeType.REMOVE_PARAMETER, name, "");
    }

    public static ModelDifference createAddedParameter(ParameterModel p2, String name) {
        return new ParameterDifference(p2, ChangeType.ADD_PARAMETER, "", name);
    }

    @Override
    public String getNodeName() {
        return parameter1.getParent().getNodePath();
    }

    @Override
    public String getParameterName() {
        return parameter1.getName();
    }

    public ParameterModel getParameter1() {
        return parameter1;
    }

    public ParameterModel getParameter2() {
        return parameter2;
    }

    public void mergeDifference() {
        switch (changeType) {
            case MODIFY_PARAMETER:
                Utils.copyBean(parameter2, parameter1);
                break;
            default:
                throw new IllegalArgumentException("NO ACTION CHANGE TYPE " + changeType.toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterDifference{");
        sb.append("parameter1='").append(parameter1.getName()).append('\'');
        sb.append(", parameter2='").append(parameter2.getName()).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", attributes='").append(attribute).append('\'');
        sb.append(", values1='").append(value1).append('\'');
        sb.append(", values2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
