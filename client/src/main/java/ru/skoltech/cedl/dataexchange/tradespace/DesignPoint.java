package ru.skoltech.cedl.dataexchange.tradespace;

import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class DesignPoint {

    private String description;

    private Epoch epoch;

    private ModelStateLink modelStateLink;

    private List<FigureOfMeritValue> values;

    public DesignPoint(Epoch epoch, List<FigureOfMeritValue> values) {
        this.epoch = epoch;
        this.values = values;
    }

    public DesignPoint(String description, Epoch epoch, List<FigureOfMeritValue> values) {
        this.description = description;
        this.epoch = epoch;
        this.values = values;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }

    public ModelStateLink getModelStateLink() {
        return modelStateLink;
    }

    public void setModelStateLink(ModelStateLink modelStateLink) {
        this.modelStateLink = modelStateLink;
    }

    public List<FigureOfMeritValue> getValues() {
        return values;
    }

    public void setValues(List<FigureOfMeritValue> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DesignPoint that = (DesignPoint) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!epoch.equals(that.epoch)) return false;
        if (modelStateLink != null ? !modelStateLink.equals(that.modelStateLink) : that.modelStateLink != null) return false;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + epoch.hashCode();
        result = 31 * result + (modelStateLink != null ? modelStateLink.hashCode() : 0);
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DesignPoint{" +
                "description='" + description + '\'' +
                ", epoch=" + epoch +
                ", modelStateLink=" + modelStateLink +
                ", values=" + values +
                '}';
    }
}
