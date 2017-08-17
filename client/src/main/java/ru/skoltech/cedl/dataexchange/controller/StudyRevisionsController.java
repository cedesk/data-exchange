/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.service.StudyService;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static ru.skoltech.cedl.dataexchange.Utils.TIME_AND_DATE_FOR_USER_INTERFACE;

/**
 * Controller for tagging a study.
 *
 * Created by Nikolay Groshkov on 16-Aug-17.
 */
public class StudyRevisionsController implements Initializable, Applicable, Displayable {

    @FXML
    private TableColumn<Pair< CustomRevisionEntity, RevisionType>, String> dateColumn;
    @FXML
    private TableColumn<Pair<CustomRevisionEntity, RevisionType>, String> tagColumn;
    @FXML
    private TableColumn<Pair<CustomRevisionEntity, RevisionType>, String> revisionColumn;
    @FXML
    private TableView<Pair<CustomRevisionEntity, RevisionType>> tagTableView;
    @FXML
    private Button loadButton;

    private StudyService studyService;
    private Study study;
    private Stage ownerStage;
    private EventHandler<Event> applyEventHandler;


    private StudyRevisionsController() {
    }

    public StudyRevisionsController(Study study) {
        this.study = study;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadButton.disableProperty().bind(Bindings.isEmpty(tagTableView.getSelectionModel().getSelectedItems()));

        List<Pair<CustomRevisionEntity, RevisionType>> items = studyService.findAllStudyRevisionEntityWithTags(study);
        tagTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tagTableView.setItems(FXCollections.observableArrayList(items));
        dateColumn.setCellValueFactory(param -> {
            Date date = param.getValue().getLeft().getRevisionDate();
            String formatterDate = TIME_AND_DATE_FOR_USER_INTERFACE.format(date);
            return new SimpleStringProperty(formatterDate);
        });
        tagColumn.setCellValueFactory(param -> {
            String tag = param.getValue().getLeft().getTag();
            return new SimpleStringProperty(tag);
        });
        revisionColumn.setCellValueFactory(param -> {
            int revision = param.getValue().getLeft().getId();
            return new SimpleStringProperty(String.valueOf(revision));
        });
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    public void load() {
        Pair<CustomRevisionEntity, RevisionType> item = tagTableView.getSelectionModel().getSelectedItem();
        CustomRevisionEntity customRevisionEntity = item.getLeft();
        if (applyEventHandler != null) {
            Event event = new Event(customRevisionEntity, null, null);
            applyEventHandler.handle(event);
        }
        this.close();
    }

    public void close() {
        this.ownerStage.close();
    }

}
