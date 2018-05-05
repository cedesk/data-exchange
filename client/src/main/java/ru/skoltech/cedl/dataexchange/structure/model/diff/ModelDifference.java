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

import ru.skoltech.cedl.dataexchange.entity.PersistedEntity;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.util.Objects;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public abstract class ModelDifference {

    protected String attribute;
    protected ChangeType changeType;
    protected ChangeLocation changeLocation;
    protected String value1;
    protected String value2;
    protected String author;

    public String getAttribute() {
        return attribute;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ChangeLocation getChangeLocation() {
        return changeLocation;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    abstract public PersistedEntity getChangedEntity();

    abstract public String getElementPath();

    abstract public ModelNode getParentNode();

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    abstract public boolean isMergeable();

    abstract public boolean isRevertible();

    abstract public void mergeDifference() throws MergeException;

    abstract public void revertDifference() throws MergeException;

    @Override
    public String toString() {
        return "ModelDifference{" + "attribute" + attribute +
                ", changeType=" + changeType +
                ", changeLocation=" + changeLocation +
                ", value1='" + value1 + '\'' +
                ", value2='" + value2 + '\'' +
                ", author='" + author + '\'' +
                "}\n ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelDifference)) return false;
        ModelDifference that = (ModelDifference) o;
        return Objects.equals(attribute, that.attribute) &&
                changeType == that.changeType &&
                changeLocation == that.changeLocation &&
                Objects.equals(value1, that.value1) &&
                Objects.equals(value2, that.value2) &&
                Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, changeType, changeLocation, value1, value2, author);
    }

    public enum ChangeType {
        ADD,
        REMOVE,
        MODIFY
    }

    public enum ChangeLocation {
        ARG1,
        ARG2
    }
}
