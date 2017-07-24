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

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

/**
 * Operations with {@link Study}.
 *
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface StudyService {

    /**
     * Create {@link Study} based on {@link SystemModel} and {@link UserManagement}.
     *
     * @param systemModel
     * @param userManagement
     * @return new instance of {@link Study}
     */
    Study createStudy(SystemModel systemModel, UserManagement userManagement);
}
