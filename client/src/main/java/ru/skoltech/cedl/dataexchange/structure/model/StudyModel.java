package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class StudyModel {

    private String repositoryPath;

    private SystemModel systemModel;

    private BooleanProperty loaded = new SimpleBooleanProperty(false);

    private BooleanProperty dirty = new SimpleBooleanProperty(false);

    private BooleanProperty checkedOut = new SimpleBooleanProperty(false);

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public SystemModel getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
        setDirty(true);
        setLoaded(true);
    }

    public boolean getLoaded() {
        return loaded.get();
    }

    public BooleanProperty loadedProperty() {
        return loaded;
    }

    public boolean getDirty() {
        return dirty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public boolean getCheckedOut() {
        return checkedOut.get();
    }

    public BooleanProperty checkedOutProperty() {
        return checkedOut;
    }

    public boolean isLoaded() {
        return loaded.get();
    }

    public void setLoaded(boolean loaded) {
        this.loaded.setValue(loaded);
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void setDirty(boolean dirty) {
        this.dirty.setValue(dirty);
    }

    public boolean isCheckedOut() {
        return checkedOut.get();
    }

    public void setCheckedOut(boolean checkedOut) {
        this.checkedOut.setValue(checkedOut);
    }
}
