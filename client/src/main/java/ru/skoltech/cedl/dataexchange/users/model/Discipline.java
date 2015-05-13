package ru.skoltech.cedl.dataexchange.users.model;

/**
 * Created by dknoll on 13/05/15.
 */
public class Discipline {

    private String disciplineName;

    private String description;

    public Discipline() {
    }

    public Discipline(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public String getDisciplineName() {
        return disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Discipline{");
        sb.append("disciplineName='").append(disciplineName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
