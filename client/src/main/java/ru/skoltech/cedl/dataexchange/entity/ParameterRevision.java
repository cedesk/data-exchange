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

import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class ParameterRevision {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ParameterModel parameterModel;
    private CustomRevisionEntity customRevisionEntity;

    public ParameterRevision(ParameterModel versionedParameterModel, CustomRevisionEntity revisionEntity) {
        this.parameterModel = versionedParameterModel;
        this.customRevisionEntity = revisionEntity;

        // TODO: bugs in Hibernate envers https://hibernate.atlassian.net/browse/HHH-8051
        this.parameterModel.setParent(null);
        if (this.parameterModel.getValueLink() != null) {
            this.parameterModel.getValueLink().setParent(null);
        }
    }

    public boolean getIsReferenceValueOverridden() {
        return parameterModel.getIsReferenceValueOverridden();
    }

    public String getName() {
        return parameterModel.getName();
    }

    public ParameterNature getNature() {
        return parameterModel.getNature();
    }

    public String getNodePath() {
        return parameterModel.getNodePath();
    }

    public Double getOverrideValue() {
        return parameterModel.getOverrideValue();
    }

    public String getRevisionAuthor() {
        return customRevisionEntity.getUsername();
    }

    public Date getRevisionDate() {
        return customRevisionEntity.getRevisionDate();
    }

    public String getRevisionDateAsText() {
        return DATE_FORMAT.format(this.getRevisionDate());
    }

    public int getRevisionId() {
        return customRevisionEntity.getId();
    }

    public String getSourceDetails() {
        if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE
                && parameterModel.getImportModel() != null
                && parameterModel.getImportField() != null) {
            return parameterModel.getImportModel() != null ?
                    parameterModel.getImportModel().getName() + ":" + parameterModel.getImportField() : "(empty)";
        } else if (parameterModel.getValueSource() == ParameterValueSource.LINK && parameterModel.getValueLink() != null) {
            return parameterModel.getValueLink().getNodePath();
        } else if (parameterModel.getValueSource() == ParameterValueSource.CALCULATION && parameterModel.getCalculation() != null) {
            return parameterModel.getCalculation().asText();
        }
        return "";
    }

    public String getUnitAsText() {
        Unit unit = parameterModel.getUnit();
        return unit != null ? unit.asText() : "";
    }

    public Double getValue() {
        return parameterModel.getValue();
    }

    public ParameterModel getValueLink() {
        return parameterModel.getValueLink();
    }

    public ParameterValueSource getValueSource() {
        return parameterModel.getValueSource();
    }
}
