package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.ModificationTimestamped;

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

    protected static boolean firstIsNewer(ModificationTimestamped arg1, ModificationTimestamped arg2) {
        Long mod1 = arg1.getLastModification();
        Long mod2 = arg2.getLastModification();
        mod1 = mod1 != null ? mod1 : 0L;
        mod2 = mod2 != null ? mod2 : 0L;
        return mod1 > mod2;
    }

    abstract public String getNodeName();

    abstract public String getParameterName();

    abstract public boolean isMergeable();

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
}
