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

package ru.skoltech.cedl.dataexchange.structure.model.diff;

/**
 * Created by D.Knoll on 18.09.2015.
 */
public class AttributeDifference {
    public String attributeName;
    public String value1;
    public String value2;

    public AttributeDifference(String attributeName, Object value1, Object value2) {
        this.attributeName = attributeName;
        this.value1 = String.valueOf(value1);
        this.value2 = String.valueOf(value2);
    }

    public String asText() {
        return "[" + attributeName + ": v1='" + value1 + '\'' +
                ", v2='" + value2 + "']";
    }

    @Override
    public String toString() {
        return "AttributeDifference{" +
                "attributeName='" + attributeName + '\'' +
                ", value1='" + value1 + '\'' +
                ", value2='" + value2 + '\'' +
                '}';
    }
}
