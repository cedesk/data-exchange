package ru.skoltech.cedl.dataexchange.repository;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class RepositoryException extends Exception {

    private String entityClassName;

    private String entityIdentifier;

    private String entityAsString;

    private String entityName;

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public String getEntityAsString() {
        return entityAsString;
    }

    public void setEntityAsString(String entityAsString) {
        this.entityAsString = entityAsString;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
