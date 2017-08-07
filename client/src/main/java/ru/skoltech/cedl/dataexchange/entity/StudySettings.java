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

package ru.skoltech.cedl.dataexchange.entity;

import javax.persistence.*;

/**
 * Created by D.Knoll on 31.10.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class StudySettings {

    private long id;

    private boolean isSyncEnabled = true;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getSyncEnabled() {
        return isSyncEnabled;
    }

    public void setSyncEnabled(boolean isSyncEnabled) {
        this.isSyncEnabled = isSyncEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudySettings that = (StudySettings) o;

        return isSyncEnabled == that.isSyncEnabled;
    }

    @Override
    public int hashCode() {
        return (isSyncEnabled ? 1 : 0);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StudySettings{");
        sb.append("isSyncEnabled=").append(isSyncEnabled);
        sb.append('}');
        return sb.toString();
    }
}
