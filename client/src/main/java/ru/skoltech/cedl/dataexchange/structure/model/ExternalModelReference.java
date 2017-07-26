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

import javax.persistence.Column;
import javax.xml.bind.annotation.*;
import java.util.Objects;

/**
 * Created by D.Knoll on 08.07.2015.
 */
@XmlType(propOrder = {"externalModel", "target"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalModelReference {

    @XmlIDREF
    @XmlAttribute
    private ExternalModel externalModel;

    @XmlAttribute
    private String target;

    public ExternalModelReference() {
    }

    public ExternalModelReference(ExternalModel externalModel, String target) {
        this.externalModel = externalModel;
        this.target = target;
    }

    public static ExternalModelReference valueOf(String string, ModelNode modelNode) {
        Objects.requireNonNull(string);
        String[] parts = string.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("argument must be of format '<name>:<coordinates>'");
        String externalModelName = parts[0];
        String target = parts[1];
        ExternalModel externalModel = findExternalModel(externalModelName, modelNode);
        return new ExternalModelReference(externalModel, target);
    }

    private static ExternalModel findExternalModel(String externalModelName, ModelNode parent) {
        for (ExternalModel externalModel : parent.getExternalModels()) {
            if (externalModel.getName().equals(externalModelName))
                return externalModel;
        }
        throw new IllegalArgumentException("invalid external model name");
    }

    public ExternalModel getExternalModel() {
        return externalModel;
    }

    public void setExternalModel(ExternalModel externalModel) {
        this.externalModel = externalModel;
    }

    @Column(nullable = true)
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalModelReference that = (ExternalModelReference) o;
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        int result = externalModel != null ? externalModel.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (externalModel == null) {
            return "(empty)";
        } else {
            return externalModel.getName() + ":" + target;
        }
    }
}
