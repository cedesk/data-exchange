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

package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 25.06.2015.
 */
public interface ModificationTimestamped {

    Long getLastModification();

    void setLastModification(Long timestamp);

    default boolean isNewerThan(ModificationTimestamped other) {
        Long mod1 = this.getLastModification();
        Long mod2 = other.getLastModification();
        mod1 = mod1 != null ? mod1 : 0L;
        mod2 = mod2 != null ? mod2 : 0L;
        return mod1 > mod2;
    }
}
