package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.util.List;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewParameters {

    private ObservableList<ParameterModel> items = FXCollections.observableArrayList();

    public ObservableList<ParameterModel> getItems() {
        return items;
    }

    public void displayParameters(List<ParameterModel> parameters) {
        items.clear();
        items.addAll(parameters);
    }
}
