package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import ru.skoltech.cedl.dataexchange.structure.ModelDifferencesFactory;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ModelDifference;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class DiffController implements Initializable {

    public TableView diffTable;
    private SystemModel localSystemModel;
    private SystemModel remoteSysteModel;
    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    public void setSystemModels(SystemModel local, SystemModel remote) {
        this.localSystemModel = local;
        this.remoteSysteModel = remote;
        modelDifferences.clear();
        List<ModelDifference> modelDiffs = ModelDifferencesFactory.computeDifferences(localSystemModel, remoteSysteModel);
        modelDifferences.addAll(modelDiffs);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(modelDifferences);
    }
}
