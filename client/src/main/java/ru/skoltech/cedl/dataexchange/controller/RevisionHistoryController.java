package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class RevisionHistoryController implements Initializable {

    @FXML
    public TableView<ParameterRevision> revisionHistoryTable;

    @FXML
    public TableColumn revisionNumberColumn;

    @FXML
    public TableColumn revisionTimestampColumn;

    @FXML
    public TableColumn parameterNameColumn;

    @FXML
    public TableColumn parameterValueColumn;

    @FXML
    public TableColumn revisionAuthorColumn;

    private ParameterModel parameter;
    private Repository repository;

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public ParameterModel getParameter() {
        return parameter;
    }

    public void setParameter(ParameterModel parameter) {
        this.parameter = parameter;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        revisionTimestampColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ParameterRevision, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures<ParameterRevision, String> parameterRevision) {
                        SimpleStringProperty property = new SimpleStringProperty();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        property.setValue(dateFormat.format(parameterRevision.getValue().getRevisionDate()));
                        return property;
                    }
                });

        ObservableList<ParameterRevision> items = FXCollections.observableArrayList();
        revisionHistoryTable.setItems(items);
    }

    public void updateView() {
        if (repository != null && parameter != null) {
            List<ParameterRevision> revisionList = repository.getChangeHistory(parameter);
            revisionHistoryTable.getItems().clear();
            revisionHistoryTable.getItems().addAll(revisionList);
        }
    }
}
