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

package ru.skoltech.cedl.dataexchange.service;

import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nikolay Groshkov on 07-Jul-17.
 */
public interface ModelUpdateService {

    /**
     * TODO add javadoc
     *
     * @param project
     * @param externalModel
     * @param externalModelFileHandler
     * @param modelUpdateListeners
     * @param parameterUpdateListener
     * @throws ExternalModelException
     */
    void applyParameterChangesFromExternalModel(Project project, ExternalModel externalModel,
                                                ParameterLinkRegistry parameterLinkRegistry,
                                                ExternalModelFileHandler externalModelFileHandler,
                                                List<? extends Consumer<ModelUpdate>> modelUpdateListeners,
                                                Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException;

    /**
     * TODO add javadoc
     *
     * @param project
     * @param parameterModel
     * @param externalModelFileHandler
     * @param parameterUpdateListener
     * @throws ExternalModelException
     */
    void applyParameterChangesFromExternalModel(Project project, ParameterModel parameterModel,
                                                ParameterLinkRegistry parameterLinkRegistry,
                                                ExternalModelFileHandler externalModelFileHandler,
                                                Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException;

    /**
     * TODO add javadoc
     *
     * @param project
     * @param externalModel
     * @param externalModelFileHandler
     * @param externalModelFileWatcher
     * @throws ExternalModelException
     */
    void applyParameterChangesToExternalModel(Project project, ExternalModel externalModel,
                                              ExternalModelFileHandler externalModelFileHandler,
                                              ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException;
}
