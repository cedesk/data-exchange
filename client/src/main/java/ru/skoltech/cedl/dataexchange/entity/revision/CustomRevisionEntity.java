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

package ru.skoltech.cedl.dataexchange.entity.revision;

import org.hibernate.envers.DefaultTrackingModifiedEntitiesRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.text.DateFormat;

/**
 * Revision entity extension.
 * Along with basic revision information it stores
 * the name of user who prodused change
 * and can store a specific tag of revision.
 * <p>
 * Created by D.Knoll on 22.06.2015.
 */
@Entity
@Table(name = "REVINFO")
@RevisionEntity
public class CustomRevisionEntity extends DefaultTrackingModifiedEntitiesRevisionEntity {

    private static final long serialVersionUID = -1255842407304108513L;

    private String username;
    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomRevisionEntity)) return false;
        if (!super.equals(o)) return false;

        CustomRevisionEntity that = (CustomRevisionEntity) o;

        if (!username.equals(that.username)) return false;
        return tag != null ? tag.equals(that.tag) : that.tag == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "CustomRevisionEntity(id = " + getId() +
                ", user = " + username +
                ", tag = " + tag +
                ", revisionDate = " + DateFormat.getDateTimeInstance().format(this.getRevisionDate()) +
                ", entityNames=" + getModifiedEntityNames() + ")";
    }

}