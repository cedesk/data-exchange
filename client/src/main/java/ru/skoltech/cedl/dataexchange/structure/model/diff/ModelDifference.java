package ru.skoltech.cedl.dataexchange.structure.model.diff;

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public abstract class ModelDifference {

    protected String attribute;
    protected ChangeType changeType;
    protected ChangeLocation changeLocation;
    protected String value1;
    protected String value2;
    protected String author;

    abstract public ModelNode getParentNode();

    abstract public String getNodeName();

    abstract public String getParameterName();

    abstract public boolean isMergeable();

    abstract public void mergeDifference();

    abstract public PersistedEntity getChangedEntity();

    public String getAttribute() {
        return attribute;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public ChangeLocation getChangeLocation() {
        return changeLocation;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean hasChangeOnSecond() {
        return changeLocation == ModelDifference.ChangeLocation.ARG2;
    }

    public boolean hasChangeOnFirst() {
        return changeLocation == ModelDifference.ChangeLocation.ARG1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelDifference{");
        sb.append("attribute").append(attribute);
        sb.append(", changeType=").append(changeType);
        sb.append(", changeLocation=").append(changeLocation);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }

    public enum ChangeType {
        ADD,
        REMOVE,
        MODIFY
    }

    public enum ChangeLocation {
        ARG1,
        ARG2
    }
}
