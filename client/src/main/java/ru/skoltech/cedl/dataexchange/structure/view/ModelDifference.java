package ru.skoltech.cedl.dataexchange.structure.view;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifference {

    private String nodePath;

    private ChangeType changeType;

    private String changeValue;

    private String author;

    public ModelDifference(String nodePath, ChangeType changeType, String changeValue) {
        this.nodePath = nodePath;
        this.changeType = changeType;
        this.changeValue = changeValue;
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

    public String getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(String changeValue) {
        this.changeValue = changeValue;
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
        sb.append(", changeValue='").append(changeValue).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
