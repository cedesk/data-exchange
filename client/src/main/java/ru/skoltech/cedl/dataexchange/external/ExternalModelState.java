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
 * <p>
 * Created by D.Knoll on 08.07.2015.
 */
public enum ExternalModelState {

    /**
     * External model dos not contain any data.
     */
    EMPTY,

    /**
     * State of external model is incorrect.
     */
    INCORRECT,

    /**
     * External model attachment is empty or null.
     */
    UNINITIALIZED,

    /**
     * There is no cache file.
     */
    NO_CACHE,

    /**
     * Both file system cache and repository are in the same state.
     */
    CACHE,

    /**
     * Both modified in repository and in local file system.
     */
    CACHE_CONFLICT,

    /**
     * Cache file was modified only in local file system.
     */
    CACHE_MODIFIED,

    /**
     * External model was modified only in repository.
     */
    CACHE_OUTDATED;

    /**
     * Determine the existence of cache file.
     *
     * @return <i>true</i> if cache file is exist and <i>false</i> if opposite.
     */
    public boolean isCached(){
        return this == CACHE || this == CACHE_CONFLICT || this == CACHE_MODIFIED || this == CACHE_OUTDATED;
    }

}
