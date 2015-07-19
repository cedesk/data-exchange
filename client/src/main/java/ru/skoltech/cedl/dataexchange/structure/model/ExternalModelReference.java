package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.*;
import java.util.Objects;

/**
 * Created by D.Knoll on 08.07.2015.
 */
@XmlType(propOrder = {"externalModel", "target"})
@XmlAccessorType(XmlAccessType.FIELD)
@Embeddable
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

    @ManyToOne(targetEntity = ExternalModel.class, optional = true)
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

        if (externalModel != null ? !externalModel.equals(that.externalModel) : that.externalModel != null)
            return false;
        return !(target != null ? !target.equals(that.target) : that.target != null);
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
            return "";
        } else {
            return externalModel.getName() + ":" + target;
        }
    }
}
