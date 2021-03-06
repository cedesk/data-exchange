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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.service.UserService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for user editing view.
 * <p>
 * Created by D.Knoll on 27.08.2015.
 */
public class UserEditingController implements Initializable {

    @FXML
    private TextField userNameText;

    @FXML
    private TextField fullNameText;

    private UserService userService;
    private User user;

    private UserEditingController() {
    }

    public UserEditingController(User userModel) {
        this.user = userModel;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (user != null) {
            userNameText.setText(user.getUserName());
            fullNameText.setText(user.getFullName());
        }
    }

    public void applyAndClose(ActionEvent actionEvent) {
        user.setUserName(userNameText.getText());
        user.setFullName(fullNameText.getText());
        userService.saveUser(user);
        this.cancel(actionEvent);
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
