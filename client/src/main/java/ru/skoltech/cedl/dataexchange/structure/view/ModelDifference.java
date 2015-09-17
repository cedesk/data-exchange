package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public abstract class ModelDifference {

    protected String attribute;

    protected ChangeType changeType;

    protected String value1;

    protected String value2;

    protected String author;

    abstract public String getNodeName();

    abstract public String getNodePath();

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelDifference{");
        sb.append("attribute").append(attribute);
        sb.append(", changeType=").append(changeType);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
