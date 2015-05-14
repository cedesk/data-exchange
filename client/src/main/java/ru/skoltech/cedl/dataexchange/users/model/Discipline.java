package ru.skoltech.cedl.dataexchange.users.model;

/**
 * Created by dknoll on 13/05/15.
 */
public class Discipline {

    public static Discipline ADMIN_DISCIPLINE = new Discipline("Admin", true);

    private String name;

    private String description;

    private boolean builtIn = false;

    public Discipline() {
    }

    private Discipline(String name, boolean builtIn) {
        this.name = name;
        this.builtIn = builtIn;
    }

    public Discipline(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Discipline{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Discipline otherDiscipline = (Discipline) other;

        if (builtIn != otherDiscipline.builtIn) return false;
        return !(name != null ? !name.equals(otherDiscipline.name) : otherDiscipline.name != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (builtIn ? 1 : 0);
        return result;
    }
}
