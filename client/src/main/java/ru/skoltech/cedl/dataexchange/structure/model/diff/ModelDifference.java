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

    public String getAttribute() {
        return attribute;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ChangeLocation getChangeLocation() {
        return changeLocation;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    abstract public PersistedEntity getChangedEntity();

    abstract public String getElementPath();

    abstract public ModelNode getParentNode();

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    abstract public boolean isMergeable();

    abstract public boolean isRevertible();

    abstract public void mergeDifference();

    abstract public void revertDifference();

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
