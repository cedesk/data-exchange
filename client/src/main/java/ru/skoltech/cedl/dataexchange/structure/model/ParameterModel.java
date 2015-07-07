package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@XmlType(propOrder = {"name", "value", "nature", "valueSource", "valueReference", "isExported", "exportReference", "lastModification", "description"})
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

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Double value;

    @XmlTransient
    private Double serverValue;

    @XmlAttribute
    private ParameterNature nature = DEFAULT_NATURE;

    @XmlAttribute
    private ParameterValueSource valueSource = DEFAULT_VALUE_SOURCE;

    private String valueReference;

    @XmlAttribute
    private boolean isReferenceValueOverridden = DEFAULT_OVERRIDDEN;

    @XmlAttribute
    private Double overrideValue;

    @XmlAttribute
    private boolean isExported = DEFAULT_EXPORTED;

    private String exportReference;

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

    public ParameterModel(String name, Double value, ParameterValueSource valueSource, boolean isExported, String description) {
        this.name = name;
        this.value = value;
        this.valueSource = valueSource;
        this.isExported = isExported;
        this.description = description;
    }

    @Transient
    public Double getEffectiveValue() {
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

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @Transient
    public Double getServerValue() {
        return serverValue;
    }

    public void setServerValue(Double serverValue) {
        this.serverValue = serverValue;
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

    public String getValueReference() {
        return valueReference;
    }

    public void setValueReference(String valueReference) {
        this.valueReference = valueReference;
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

    public String getExportReference() {
        return exportReference;
    }

    public void setExportReference(String exportReference) {
        this.exportReference = exportReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(targetEntity = ModelNode.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    public boolean hasServerChange() {
        if (getServerValue() != null) {
            // TODO: account for floating point comparison with imprecision
            return !getValue().equals(getServerValue());
        } else {
            return false;
        }
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

        if (isReferenceValueOverridden != that.isReferenceValueOverridden) return false;
        if (isExported != that.isExported) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (nature != that.nature) return false;
        if (valueSource != that.valueSource) return false;
        if (valueReference != null ? !valueReference.equals(that.valueReference) : that.valueReference != null)
            return false;
        if (overrideValue != null ? !overrideValue.equals(that.overrideValue) : that.overrideValue != null)
            return false;
        if (exportReference != null ? !exportReference.equals(that.exportReference) : that.exportReference != null)
            return false;
        return !(description != null ? !description.equals(that.description) : that.description != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (nature != null ? nature.hashCode() : 0);
        result = 31 * result + (valueSource != null ? valueSource.hashCode() : 0);
        result = 31 * result + (valueReference != null ? valueReference.hashCode() : 0);
        result = 31 * result + (isReferenceValueOverridden ? 1 : 0);
        result = 31 * result + (overrideValue != null ? overrideValue.hashCode() : 0);
        result = 31 * result + (isExported ? 1 : 0);
        result = 31 * result + (exportReference != null ? exportReference.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value=").append(value);
        sb.append(", serverValue=").append(serverValue);
        sb.append(", nature=").append(nature);
        sb.append(", valueSource=").append(valueSource);
        sb.append(", valueReference=").append(valueReference);
        sb.append(", isReferenceValueOverridden=").append(isReferenceValueOverridden);
        sb.append(", overrideValue=").append(overrideValue);
        sb.append(", isExported=").append(isExported);
        sb.append(", exportReference=").append(exportReference);
        sb.append(", version=").append(version);
        sb.append(", description='").append(description).append('\'');
        sb.append(", lastModification='").append(lastModification).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
