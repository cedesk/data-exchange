package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.skoltech.cedl.dataexchange.units.UnitAdapter;
import ru.skoltech.cedl.dataexchange.units.model.Unit;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.UUID;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@XmlType(propOrder = {"name", "value", "nature", "valueSource", "unit", "isExported", "isReferenceValueOverridden", "lastModification", "uuid", "valueReference", "valueLink", "exportReference", "calculation", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
@Audited
public class ParameterModel implements Comparable<ParameterModel>, ModificationTimestamped {

    public static final ParameterNature DEFAULT_NATURE = ParameterNature.INTERNAL;

    public static final ParameterValueSource DEFAULT_VALUE_SOURCE = ParameterValueSource.MANUAL;

    public static final boolean DEFAULT_EXPORTED = false;

    public static final boolean DEFAULT_OVERRIDDEN = false;

    @XmlTransient
    private long id;

    @XmlID
    @XmlAttribute
    private String uuid = UUID.randomUUID().toString();

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Double value;

    @XmlAttribute
    @XmlJavaTypeAdapter(value = UnitAdapter.class)
    private Unit unit;

    @XmlAttribute
    private ParameterNature nature = DEFAULT_NATURE;

    @XmlAttribute
    private ParameterValueSource valueSource = DEFAULT_VALUE_SOURCE;

    private ExternalModelReference valueReference;

    @XmlIDREF
    private ParameterModel valueLink;

    private Calculation calculation;

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

    private ExternalModelReference exportReference;

    @XmlTransient
    private ExternalModel exportModel;

    @XmlTransient
    private String exportField;

    private String description;

    @XmlTransient
    private long version;

    @XmlAttribute
    private Long lastModification;

    @XmlTransient
    private ModelNode parent;

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

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @ManyToOne(targetEntity = Unit.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public ParameterNature getNature() {
        return nature == null ? DEFAULT_NATURE : nature;
    }

    public void setNature(ParameterNature nature) {
        this.nature = nature;
    }

    public ParameterValueSource getValueSource() {
        return valueSource == null ? DEFAULT_VALUE_SOURCE : valueSource;
    }

    public void setValueSource(ParameterValueSource valueSource) {
        this.valueSource = valueSource;
    }

    @Transient
    public ExternalModelReference getValueReference() {
        if (importModel == null || importField == null)
            return null;
        if (valueReference == null) {
            valueReference = new ExternalModelReference();
        }
        valueReference.setExternalModel(importModel);
        valueReference.setTarget(importField);
        return valueReference;
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

    @ManyToOne(targetEntity = ParameterModel.class)
    public ParameterModel getValueLink() {
        return valueLink;
    }

    public void setValueLink(ParameterModel valueLink) {
        this.valueLink = valueLink;
    }

    @OneToOne(targetEntity = Calculation.class, orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public Calculation getCalculation() {
        return calculation;
    }

    public void setCalculation(Calculation calculation) {
        this.calculation = calculation;
    }

    @ManyToOne(targetEntity = ExternalModel.class, optional = true, cascade = CascadeType.ALL)
    public ExternalModel getImportModel() {
        return importModel;
    }

    public void setImportModel(ExternalModel importModel) {
        this.importModel = importModel;
    }

    public String getImportField() {
        return importField;
    }

    public void setImportField(String importField) {
        this.importField = importField;
    }

    public boolean getIsReferenceValueOverridden() {
        return isReferenceValueOverridden;
    }

    public void setIsReferenceValueOverridden(boolean isReferenceValueOverridden) {
        this.isReferenceValueOverridden = isReferenceValueOverridden;
    }

    public Double getOverrideValue() {
        return overrideValue;
    }

    public void setOverrideValue(Double overrideValue) {
        this.overrideValue = overrideValue;
    }

    public boolean getIsExported() {
        return isExported;
    }

    public void setIsExported(boolean isExported) {
        this.isExported = isExported;
    }

    @Transient
    public ExternalModelReference getExportReference() {
        if (exportModel == null || exportField == null)
            return null;
        if (exportReference == null) {
            exportReference = new ExternalModelReference();
        }
        exportReference.setExternalModel(exportModel);
        exportReference.setTarget(exportField);
        return exportReference;
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

    @ManyToOne(targetEntity = ExternalModel.class, optional = true, cascade = CascadeType.ALL)
    public ExternalModel getExportModel() {
        return exportModel;
    }

    public void setExportModel(ExternalModel exportModel) {
        this.exportModel = exportModel;
    }

    public String getExportField() {
        return exportField;
    }

    public void setExportField(String exportField) {
        this.exportField = exportField;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public double getEffectiveValue() {
        if(valueSource == ParameterValueSource.LINK && valueLink != null) {
            setValue(valueLink.getEffectiveValue());
        } else if(valueSource == ParameterValueSource.CALCULATION && calculation != null) {
            setValue(calculation.evaluate());
        }
        return isReferenceValueOverridden ? overrideValue : value;
    }

    @Version()
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public Long getLastModification() {
        return lastModification;
    }

    @Override
    public void setLastModification(Long timestamp) {
        this.lastModification = timestamp;
    }

    @ManyToOne(targetEntity = ModelNode.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    @Transient
    public String getNodePath() {
        return (parent != null ? parent.getNodePath() : "") + "::" + name;
    }

    /*
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
        int result = uuid != null ? uuid.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value=").append(value);
        sb.append(", isReferenceValueOverridden=").append(isReferenceValueOverridden);
        sb.append(", overrideValue=").append(overrideValue);
        sb.append(", unit").append(unit);
        sb.append(", nature=").append(nature);
        sb.append(", valueSource=").append(valueSource);
        sb.append(", valueReference=").append(getValueReference());
        sb.append(", valueLink=").append(valueLink != null ? valueLink.getNodePath() : null);
        sb.append(", isExported=").append(isExported);
        sb.append(", exportReference=").append(getExportReference());
        sb.append(", description='").append(description).append('\'');
        sb.append(", version=").append(version);
        sb.append(", lastModification='").append(lastModification).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
