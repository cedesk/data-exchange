package ru.skoltech.cedl.dataexchange.structure.model;

import java.io.File;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class StudyModel {

    private File file;

    private SystemModel systemModel;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public SystemModel getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
    }
}
