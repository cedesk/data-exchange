package ru.skoltech.cedl.dataexchange;

import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.io.File;

/**
 * Created by D.Knoll on 07.07.2015.
 */
public class ProjectContext {

    private static ProjectContext INSTANCE = new ProjectContext();

    private String projectName;

    private ProjectContext() {
    }

    public static ProjectContext getINSTANCE() {
        return INSTANCE;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public File getProjectDataDir() {
        return StorageUtils.getDataDir(projectName);
    }
}
