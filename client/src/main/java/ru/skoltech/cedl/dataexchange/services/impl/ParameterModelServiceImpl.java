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

package ru.skoltech.cedl.dataexchange.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;
import ru.skoltech.cedl.dataexchange.repository.ParameterModelRepository;
import ru.skoltech.cedl.dataexchange.services.ParameterModelService;

import java.util.List;

/**
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class ParameterModelServiceImpl implements ParameterModelService {

    private final ParameterModelRepository parameterModelRepository;

    @Autowired
    public ParameterModelServiceImpl(ParameterModelRepository parameterModelRepository) {
        this.parameterModelRepository = parameterModelRepository;
    }

    @Override
    public List<ParameterRevision> parameterModelChangeHistory(ParameterModel parameterModel) {
        long parameterModelId = parameterModel.getId();
        return parameterModelRepository.findRevisionsOrderByRevisionNumberDesc(parameterModelId);
    }
}
