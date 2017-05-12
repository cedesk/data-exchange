package ru.skoltech.cedl.dataexchange.structure.model.diff;

/**
 * Created by D.Knoll on 18.09.2015.
 */
public class AttributeDifference {
    public String attributeName;
    public String value1;
    public String value2;

    public AttributeDifference(String attributeName, Object value1, Object value2) {
        this.attributeName = attributeName;
        this.value1 = String.valueOf(value1);
        this.value2 = String.valueOf(value2);
    }

    @Override
    public String toString() {
        return "AttributeDifference{" +
                "attributeName='" + attributeName + '\'' +
                ", value1='" + value1 + '\'' +
                ", value2='" + value2 + '\'' +
                '}';
    }

    public String asText() {
        return "[" + attributeName + ": v1='" + value1 + '\'' +
                ", v2='" + value2 + "']";
    }
}
