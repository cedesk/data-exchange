/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
