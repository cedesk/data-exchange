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

import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.ApplicationPackage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service to monitor application's updates in the repository.
 * <p>
 * Created by Nikolay Groshkov on 18-Jul-17.
 */
public interface UpdateService {

    Optional<ApplicationPackage> getLatestVersionAvailable();

    List<Pair<String, String>> extractFileNamesAndLinks(File file) throws IOException;

    ApplicationPackage getLatest(List<Pair<String, String>> pairs);
}
