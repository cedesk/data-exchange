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

package ru.skoltech.cedl.dataexchange.external;

/**
 * Enum specifies the state of the external model cache which is
 * defined by the interrelation of the data, stored in the repository
 * and in the file system.
 *
 * Created by D.Knoll on 08.07.2015.
 */
public enum ExternalModelCacheState {
    /**
     * There is no cache file.
     */
    NOT_CACHED,

    /**
     * Both modified in repository and in local file system.
     */
    CACHED_CONFLICTING_CHANGES,

    /**
     * Cache file was modified only in local file system.
     */
    CACHED_MODIFIED_AFTER_CHECKOUT,

    /**
     * External model was modified only in repository.
     */
    CACHED_OUTDATED,

    /**
     * Both file system cache and repository are in the same state.
     */
    CACHED_UP_TO_DATE
}
