package ru.skoltech.cedl.dataexchange.structure.view;

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
}
