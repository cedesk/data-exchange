package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * Created by D.Knoll on 08.07.2015.
 */
@Embeddable
public class ExternalModelReference {

    private ExternalModel externalModel;

    private String target;

    public ExternalModelReference() {
    }

    public ExternalModelReference(ExternalModel externalModel, String target) {
        this.externalModel = externalModel;
        this.target = target;
    }

    @ManyToOne(targetEntity = ExternalModel.class)
    public ExternalModel getExternalModel() {
        return externalModel;
    }

    public void setExternalModel(ExternalModel externalModel) {
        this.externalModel = externalModel;
    }

    @Column(nullable = false)
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
