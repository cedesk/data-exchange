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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.Utils.TIME_AND_DATE_FOR_USER_INTERFACE;

/**
 * Controller for tagging a study.
 *
 * Created by Nikolay Groshkov on 16-Aug-17.
 */
public class TagController implements Initializable, Displayable {

    @FXML
    private TextField tagTextField;
    @FXML
    private TableColumn<Pair<Date, String>, String> dateColumn;
    @FXML
    private TableColumn<Pair<Date, String>, String> tagColumn;
    @FXML
    private TableView<Pair<Date, String>> tagTableView;
    @FXML
    private Button tagButton;

    private StudyService studyService;
    private Study study;
    private Stage ownerStage;

    private TagController() {
    }

    public TagController(Study study) {
        this.study = study;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tagButton.disableProperty().bind(Bindings.isEmpty(tagTextField.textProperty()));

        List<Pair<CustomRevisionEntity, RevisionType>> studyTags = studyService.findAllStudyRevisionEntityWithTags(study);
        List<Pair<Date, String>> items = studyTags.stream()
                .map(revision -> Pair.of(revision.getLeft().getRevisionDate(), revision.getLeft().getTag()))
                .collect(Collectors.toList());
        tagTableView.setItems(FXCollections.observableArrayList(items));
        tagTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tagTableView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> tagTextField.setText(newValue != null ? newValue.getRight() : null));
        dateColumn.setCellValueFactory(param -> {
            Date date = param.getValue().getLeft();
            String formatterDate = TIME_AND_DATE_FOR_USER_INTERFACE.format(date);
            return new SimpleStringProperty(formatterDate);
        });
        tagColumn.setCellValueFactory(param -> {
            String tag = param.getValue().getRight();
            return new SimpleStringProperty(tag);
        });
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    public void tag() {
        String tag = tagTextField.getText();
        studyService.untagStudy(study, tag);
        studyService.tagStudy(study, tagTextField.getText());
        this.close();
    }

    public void close() {
        this.ownerStage.close();
    }

}
