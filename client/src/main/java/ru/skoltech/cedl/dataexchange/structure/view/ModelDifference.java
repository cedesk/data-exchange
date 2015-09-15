package ru.skoltech.cedl.dataexchange.structure.view;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifference {

    private String nodePath;

    private ChangeType changeType;

    private String value1;

    private String value2;

    private String author;

    public ModelDifference(String nodePath, ChangeType changeType) {
        this.nodePath = nodePath;
        this.changeType = changeType;
    }

    public ModelDifference(String nodePath, ChangeType changeType, String value1, String value2) {
        this.nodePath = nodePath;
        this.changeType = changeType;
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
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
        sb.append("nodePath='").append(nodePath).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
