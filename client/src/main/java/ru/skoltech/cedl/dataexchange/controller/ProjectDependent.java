package ru.skoltech.cedl.dataexchange.controller;

import ru.skoltech.cedl.dataexchange.structure.Project;

/**
 * Marks a dependency on project instance.
 *
 * Created by n.groshkov on 04-Jul-17.
 */
public interface ProjectDependent {

    /**
     * Inject project instance.
     *
     * @param project instance.
     */
    void setProject(Project project);
}