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

package ru.skoltech.cedl.dataexchange.entity.tradespace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by d.knoll on 6/28/2017.
 */
@Entity
public class ModelStateLink {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    private int studyRevisionId;

    public ModelStateLink(int studyRevisionId) {
        this.studyRevisionId = studyRevisionId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStudyRevisionId() {
        return studyRevisionId;
    }

    public void setStudyRevisionId(int studyRevisionId) {
        this.studyRevisionId = studyRevisionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelStateLink modelStateLink = (ModelStateLink) o;

        return studyRevisionId == modelStateLink.studyRevisionId;
    }

    @Override
    public int hashCode() {
        return studyRevisionId;
    }

    @Override
    public String toString() {
        return "ModelStateLink{" +
                "studyRevisionId=" + studyRevisionId +
                '}';
    }
}
