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

import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.units.model.Unit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class ParameterRevision extends ParameterModel {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int revisionId;

    private Date revisionDate;

    private String revisionAuthor;

    private RevisionType revisionType;

    public ParameterRevision(ParameterModel versionedParameterModel, CustomRevisionEntity revisionEntity, RevisionType revisionType) {
        this.revisionId = revisionEntity.getId();
        this.revisionDate = revisionEntity.getRevisionDate();
        this.revisionAuthor = revisionEntity.getUsername();
        this.revisionType = revisionType;

        setId(versionedParameterModel.getId());
        setName(versionedParameterModel.getName());
        setValue(versionedParameterModel.getValue());
        setUnit(versionedParameterModel.getUnit());
        setIsReferenceValueOverridden(versionedParameterModel.getIsReferenceValueOverridden());
        setOverrideValue(versionedParameterModel.getOverrideValue());
        setDescription(versionedParameterModel.getDescription());
        setNature(versionedParameterModel.getNature());
        setIsExported(versionedParameterModel.getIsExported());
        setValueSource(versionedParameterModel.getValueSource());
        setValueReference(versionedParameterModel.getValueReference());
        setValueLink(versionedParameterModel.getValueLink());
        setCalculation(versionedParameterModel.getCalculation());
    }

    public int getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public String getRevisionAuthor() {
        return revisionAuthor;
    }

    public void setRevisionAuthor(String revisionAuthor) {
        this.revisionAuthor = revisionAuthor;
    }

    public RevisionType getRevisionType() {
        return revisionType;
    }

    public void setRevisionType(RevisionType revisionType) {
        this.revisionType = revisionType;
    }

    public String getSourceDetails() {
        if (getValueSource() == ParameterValueSource.REFERENCE && getValueReference() != null) {
            return getValueReference().toString();
        } else if (getValueSource() == ParameterValueSource.LINK && getValueLink() != null) {
            return getValueLink().getNodePath();
        } else if (getValueSource() == ParameterValueSource.CALCULATION && getCalculation() != null) {
            return getCalculation().asText();
        }
        return "";
    }

    public String getUnitAsText() {
        Unit unit = super.getUnit();
        return unit != null ? unit.asText() : "";
    }

    public String getRevisionDateAsText() {
        return dateFormat.format(getRevisionDate());
    }

    @Override
    public boolean getIsReferenceValueOverridden() {
        return super.getIsReferenceValueOverridden();
    }

    public boolean getReferenceValueOverridden() {
        return super.getIsReferenceValueOverridden();
    }

    @Override
    public int compareTo(ParameterModel o) {
        if (this == o) return 0;
        if (!(o instanceof ParameterRevision)) return -1;

        ParameterRevision that = (ParameterRevision) o;
        int comp = Integer.compare(this.revisionId, that.revisionId);
        if (comp != 0) return comp;

        return super.compareTo(that);
    }
}
