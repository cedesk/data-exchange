package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;

/**
 * Created by D.Knoll on 07.07.2015.
 */
public class ProjectContext {

    private static Logger logger = Logger.getLogger(ProjectContext.class);

    private static ProjectContext INSTANCE = new ProjectContext();

    private Project project;

    private ProjectContext() {
    }

    public static ProjectContext getInstance() {
        return INSTANCE;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
