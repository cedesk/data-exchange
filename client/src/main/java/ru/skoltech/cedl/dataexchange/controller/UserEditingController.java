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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.entity.user.User;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 27.08.2015.
 */
public class UserEditingController implements Initializable {

    @FXML
    private TextField userNameText;

    @FXML
    private TextField fullNameText;

    private User userModel;

    private UserEditingController() {
    }

    public UserEditingController(User userModel) {
        this.userModel = userModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userModel != null) {
            userNameText.setText(userModel.getUserName());
            fullNameText.setText(userModel.getFullName());
        }
    }

    public void applyAndClose(ActionEvent actionEvent) {
        boolean success = updateModel();
        if (success) {
            cancel(actionEvent);
        }
    }

    private boolean updateModel() {
        userModel.setUserName(userNameText.getText());
        userModel.setFullName(fullNameText.getText());
        return true;
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
