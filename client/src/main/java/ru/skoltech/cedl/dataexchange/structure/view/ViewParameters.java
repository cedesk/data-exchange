package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterComparatorByNatureAndName;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewParameters {

    private ObservableList<ParameterModel> items = FXCollections.observableArrayList();

    public ObservableList<ParameterModel> getItems() {
        return items;
    }

    public void displayParameters(List<ParameterModel> parameters, boolean outputOnly) {
        items.clear();
        if (outputOnly) {
            List<ParameterModel> parameterModels = parameters.stream()
                    .filter(parameterModel -> parameterModel.getNature() == ParameterNature.OUTPUT)
                    .collect(Collectors.toList());
            items.addAll(parameterModels);
        } else {
            items.addAll(parameters);
        }
        items.sort(new ParameterComparatorByNatureAndName());
    }
}
