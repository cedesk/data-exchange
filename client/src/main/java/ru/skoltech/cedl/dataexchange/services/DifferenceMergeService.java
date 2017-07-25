/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.diff.MergeException;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.List;

/**
 * Service for merging differences of projects.
 *
 * Created by Nikolay Groshkov on 07-Jul-17.
 */
public interface DifferenceMergeService {

    /**
     * TODO add javadoc
     *
     * @param project
     * @param modelDifference
     * @return
     * @throws MergeException
     */
    boolean mergeOne(Project project, ModelDifference modelDifference) throws MergeException;

    /**
     * TODO add javadoc
     *
     * @param project
     * @param modelDifference
     * @return
     * @throws MergeException
     */
    boolean revertOne(Project project, ModelDifference modelDifference) throws MergeException;

    /**
     * TODO add javadoc
     *
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    List<ModelDifference> mergeChangesOntoFirst(Project project, List<ModelDifference> modelDifferences) throws MergeException;

    /**
     * TODO add javadoc
     *
     * @param modelDifferences the list of differences to be merged, retaining only unmerged ones
     * @return the list of merged differences
     */
    List<ModelDifference> revertChangesOnFirst(Project project, List<ModelDifference> modelDifferences) throws MergeException;
}
