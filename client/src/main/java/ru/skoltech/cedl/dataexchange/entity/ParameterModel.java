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

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterReferenceValidity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.UUID;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@Entity
@Audited
@XmlType(propOrder = {"name", "value", "nature", "valueSource", "unit", "isExported", "isReferenceValueOverridden", "lastModification", "uuid", "valueReference", "valueLink", "exportReference", "calculation", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ParameterModel implements Comparable<ParameterModel>, PersistedEntity {

    private static Logger logger = Logger.getLogger(ParameterModel.class);

    private static final ParameterNature DEFAULT_NATURE = ParameterNature.INTERNAL;
    private static final ParameterValueSource DEFAULT_VALUE_SOURCE = ParameterValueSource.MANUAL;
    private static final boolean DEFAULT_EXPORTED = false;
    private static final boolean DEFAULT_OVERRIDDEN = false;

    @Id
    @GeneratedValue
    @XmlTransient
    private long id;

    @Revision
    @NotAudited
    @XmlTransient
    private int revision;

    @XmlID
    @XmlAttribute
    private String uuid = UUID.randomUUID().toString();

    @Column(nullable = false)
    @XmlAttribute
    private String name;

    @XmlAttribute
    private Double value;

    @ManyToOne(targetEntity = Unit.class, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @XmlAttribute
    @XmlJavaTypeAdapter(value = UnitAdapter.class)
    private Unit unit;

    @XmlAttribute
    private ParameterNature nature = DEFAULT_NATURE;

    @XmlAttribute
    private ParameterValueSource valueSource = DEFAULT_VALUE_SOURCE;

    @Transient
    private ExternalModelReference valueReference;

    @ManyToOne(targetEntity = ParameterModel.class, fetch = FetchType.EAGER)
    @XmlIDREF
    private ParameterModel valueLink;

    @OneToOne(targetEntity = Calculation.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Calculation calculation;

    @ManyToOne(targetEntity = ExternalModel.class, cascade = CascadeType.ALL)
    @XmlTransient
    private ExternalModel importModel;

    @XmlTransient
    private String importField;

    @XmlAttribute
    private boolean isReferenceValueOverridden = DEFAULT_OVERRIDDEN;

    @XmlAttribute
    private Double overrideValue;

    @XmlAttribute
    private boolean isExported = DEFAULT_EXPORTED;

    @Transient
    private ExternalModelReference exportReference;

    @ManyToOne(targetEntity = ExternalModel.class, cascade = CascadeType.ALL)
    @XmlTransient
    private ExternalModel exportModel;

    @XmlTransient
    private String exportField;

    private String description;

    @XmlAttribute
    private Long lastModification;

    @ManyToOne(targetEntity = ModelNode.class, fetch = FetchType.EAGER)
    @XmlTransient
    private ModelNode parent;

    @Transient
    @XmlTransient
    private ParameterModelUpdateState lastParameterModelUpdateState;

    public ParameterModel() {
    }

    public ParameterModel(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    public ParameterModel(String name, Double value, ParameterNature nature, ParameterValueSource valueSource) {
        this.name = name;
        this.value = value;
        this.nature = nature;
        this.valueSource = valueSource;
    }

    public ParameterModel(String name, Double value, ParameterValueSource valueSource, boolean isExported, String description) {
        this.name = name;
        this.value = value;
        this.valueSource = valueSource;
        this.isExported = isExported;
        this.description = description;
    }

    public Calculation getCalculation() {
        return calculation;
    }

    public void setCalculation(Calculation calculation) {
        this.calculation = calculation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getEffectiveValue() {
        if (valueSource == ParameterValueSource.LINK && valueLink != null) {
            return isReferenceValueOverridden ? overrideValue : valueLink.getEffectiveValue();
        } else if (valueSource == ParameterValueSource.CALCULATION && calculation != null && calculation.valid()) {
            return isReferenceValueOverridden ? overrideValue : calculation.evaluate();
        }
        Double effectiveValue = isReferenceValueOverridden ? overrideValue : value; // OUTPUT CAN BE OVERRIDDEN;
        return effectiveValue != null ? effectiveValue : Double.NaN;
    }

    public String getExportField() {
        return exportField;
    }

    public void setExportField(String exportField) {
        this.exportField = exportField;
    }

    public ExternalModel getExportModel() {
        return exportModel;
    }

    public void setExportModel(ExternalModel exportModel) {
        this.exportModel = exportModel;
    }

    public ExternalModelReference getExportReference() {
        return this.getReference(this.exportReference, this.exportModel, this.exportField);
    }

    public void setExportReference(ExternalModelReference exportReference) {
        this.exportReference = exportReference;

        if (exportReference != null) {
            this.exportModel = exportReference.getExternalModel();
            this.exportField = exportReference.getTarget();
        } else {
            this.exportModel = null;
            this.exportField = null;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getImportField() {
        return importField;
    }

    public void setImportField(String importField) {
        this.importField = importField;
    }

    public ExternalModel getImportModel() {
        return importModel;
    }

    public void setImportModel(ExternalModel importModel) {
        this.importModel = importModel;
    }

    public boolean getIsExported() {
        return isExported;
    }

    public void setIsExported(boolean isExported) {
        this.isExported = isExported;
    }

    public boolean getIsReferenceValueOverridden() {
        return isReferenceValueOverridden;
    }

    public void setIsReferenceValueOverridden(boolean isReferenceValueOverridden) {
        this.isReferenceValueOverridden = isReferenceValueOverridden;
    }

    public Long getLastModification() {
        return lastModification;
    }

    public void setLastModification(Long timestamp) {
        this.lastModification = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterNature getNature() {
        return nature == null ? DEFAULT_NATURE : nature;
    }

    public void setNature(ParameterNature nature) {
        this.nature = nature;
    }

    public String getNodePath() {
        return (parent != null ? parent.getNodePath() : "") + "::" + name;
    }

    public Double getOverrideValue() {
        return overrideValue;
    }

    public void setOverrideValue(Double overrideValue) {
        this.overrideValue = overrideValue;
    }

    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public ParameterModel getValueLink() {
        return valueLink;
    }

    public void setValueLink(ParameterModel valueLink) {
        this.valueLink = valueLink;
    }

    public ExternalModelReference getValueReference() {
        return this.getReference(this.valueReference, this.importModel, this.importField);
    }

    public void setValueReference(ExternalModelReference valueReference) {
        this.valueReference = valueReference;
        if (valueReference != null) {
            this.importModel = valueReference.getExternalModel();
            this.importField = valueReference.getTarget();
        } else {
            this.importModel = null;
            this.importField = null;
        }
    }

    public ParameterValueSource getValueSource() {
        return valueSource == null ? DEFAULT_VALUE_SOURCE : valueSource;
    }

    public void setValueSource(ParameterValueSource valueSource) {
        this.valueSource = valueSource;
    }

    /**
     * Get a result of most resent value reference update from external model.
     * Returns null if value reference is in invalid state.
     * <p/>
     * @return state of most recent value reference update of null if value reference is in invalid state
     */
    public ParameterModelUpdateState getLastValueReferenceUpdateState() {
        if (!isValidValueReference()) {
            return null;
        }
        return lastParameterModelUpdateState;
    }

    /**
     * Validate the consistency of current value reference.
     * <p/>
     * @return a validity state object of current value reference
     * (<i>null<i/> if current value source is not of type of reference)
     */
    public ParameterReferenceValidity validateValueReference() {
        if (this.getValueSource() != ParameterValueSource.REFERENCE) {
            return null;
        }
        ExternalModelReference valueReference = this.getValueReference();
        if (valueReference == null) {
            logger.warn("Parameter model" + this.getNodePath() + " has empty value reference");
            return ParameterReferenceValidity.INVALID_EMPTY_REFERENCE;
        }
        ExternalModel valueReferenceExternalModel = valueReference.getExternalModel();
        if (valueReferenceExternalModel == null) {
            logger.warn("Parameter model" + this.getNodePath() + " has empty value reference external model");
            return ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_EXTERNAL_MODEL;
        }
        if (valueReference.getTarget() == null || valueReference.getTarget().isEmpty()) {
            logger.warn("Parameter model " + this.getNodePath() + " has empty value reference target");
            return ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_TARGET;
        }
        return ParameterReferenceValidity.VALID;
    }

    /**
     * Validate the consistency of current export reference.
     * <p/>
     * @return a validity state object of current export reference
     * (<i>null<i/> if parameter model is not exported or current value source is not of type of reference)
     */
    public ParameterReferenceValidity validateExportReference() {
        if (!this.isExported || this.getValueSource() != ParameterValueSource.REFERENCE) {
            return null;
        }
        ExternalModelReference exportReference = this.getExportReference();
        if (exportReference == null) {
            logger.warn("Parameter model " + this.getNodePath() + " has empty export reference");
            return ParameterReferenceValidity.INVALID_EMPTY_REFERENCE;
        }
        if (exportReference.getExternalModel() == null) {
            logger.warn("Parameter model " + this.getNodePath() + " has empty export reference external model");
            return ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_EXTERNAL_MODEL;
        }
        if (exportReference.getTarget() == null || exportReference.getTarget().isEmpty()) {
            logger.warn("Parameter model " + this.getNodePath() + " has empty export reference target");
            return ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_TARGET;
        }
        return ParameterReferenceValidity.VALID;
    }

    /**
     * Check a validity of current value reference.
     * <p/>
     * @return <i>true</i> current value reference is valid, <i>false<i/> if opposite
     */
    public boolean isValidValueReference() {
        return this.validateValueReference() != null && this.validateValueReference().isValid();
    }

    /**
     * Check a validity of current export reference.
     * <p/>
     * @return <i>true</i> current export reference is valid, <i>false<i/> if opposite
     */
    public boolean isValidExportReference() {
        return this.validateExportReference() != null && this.validateExportReference().isValid();
    }

    /**
     * Update parameter model value with data taken from value reference external model.
     * Status of this update is saved and can be retrieved by calling {@link ParameterModel#getLastValueReferenceUpdateState()} method.
     * <p/>
     * @return <i>true</i> if parameter model has received a new correct value
     * from the data of value reference external model, <i>false<i/> if opposite
     */
    public boolean updateValueReference() {
        if (!isValidValueReference()) {
            return false;
        }

        ExternalModelReference valueReference = this.getValueReference();
        ExternalModel valueReferenceExternalModel = valueReference.getExternalModel();

        try {
            Double value = valueReferenceExternalModel.getValue(valueReference.getTarget());
            if (Double.isNaN(value)) {
                logger.warn("Parameter model " + this.getNodePath() + " evaluated invalid value");
                lastParameterModelUpdateState = ParameterModelUpdateState.FAIL_INVALID_VALUE;
                return false;
            } else if (this.getValue() != null && Precision.equals(this.getValue(), value, 2)) {
                logger.debug("Parameter model " + this.getNodePath()
                        + " received no update from " + valueReference.toString());
                lastParameterModelUpdateState = ParameterModelUpdateState.SUCCESS_WITHOUT_UPDATE;
                return false;
            } else {
                this.setValue(value);
                logger.info("Parameter model " + this.getNodePath()
                        + " successfully evaluated its value (" + String.valueOf(value) + ")");
                lastParameterModelUpdateState = ParameterModelUpdateState.SUCCESS;
                return true;
            }
        } catch (ExternalModelException e) {
            logger.warn("Parameter model " + this.getNodePath()
                    + " failed to update its value with an internal error: " + e.getMessage());
            lastParameterModelUpdateState = ParameterModelUpdateState.FAIL_EVALUATION;
            return false;
        }
    }

    private ExternalModelReference getReference(ExternalModelReference reference, ExternalModel externalModel, String target){
        if (reference != null) {
            return reference;
        }
        if (externalModel == null && target == null) {
            return null;
        }
        reference = new ExternalModelReference();
        reference.setExternalModel(externalModel);
        reference.setTarget(target);
        return reference;
    }

    /**
     * The comparison is done only based on the name, so it enables sorting of parameters by name and identifying changes to values of parameters.
     */
    @Override
    public int compareTo(ParameterModel o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterModel that = (ParameterModel) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (isReferenceValueOverridden != that.isReferenceValueOverridden) return false;
        if (overrideValue != null ? !overrideValue.equals(that.overrideValue) : that.overrideValue != null)
            return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;
        if (nature != that.nature) return false;
        if (valueSource != that.valueSource) return false;
        if (getValueReference() != null ? !getValueReference().equals(that.getValueReference()) : that.getValueReference() != null) {
            return false;
        }
        if (valueLink != null ? !valueLink.equals(that.valueLink) : that.valueLink != null)
            return false;
        if (isExported != that.isExported) return false;
        if (getExportReference() != null ? !getExportReference().equals(that.getExportReference()) : that.getExportReference() != null)
            return false;
        return !(description != null ? !description.equals(that.description) : that.description != null);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ParameterModel{" + "name='" + name + '\'' +
                ", value=" + value +
                ", isReferenceValueOverridden=" + isReferenceValueOverridden +
                ", overrideValue=" + overrideValue +
                ", unit=" + unit +
                ", nature=" + nature +
                ", valueSource=" + valueSource +
                ", valueReference=" + getValueReference() +
                ", valueLink=" + (valueLink != null ? valueLink.getNodePath() : null) +
                ", isExported=" + isExported +
                ", exportReference=" + getExportReference() +
                ", description='" + description + '\'' +
                ", lastModification='" + lastModification + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
