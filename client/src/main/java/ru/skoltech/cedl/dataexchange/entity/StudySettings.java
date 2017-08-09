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

import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * Created by D.Knoll on 31.10.2015.
 */
@Entity
@Audited(targetAuditMode = NOT_AUDITED)
public class StudySettings {

    @Id
    @GeneratedValue
    private long id;

    private boolean syncEnabled = true;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean isSyncEnabled) {
        this.syncEnabled = isSyncEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudySettings that = (StudySettings) o;

        return syncEnabled == that.syncEnabled;
    }

    @Override
    public int hashCode() {
        return (syncEnabled ? 1 : 0);
    }

    @Override
    public String toString() {
        return "StudySettings{" +
                "syncEnabled=" + syncEnabled +
                '}';
    }
}
