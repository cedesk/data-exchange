package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.users.model.User;

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

    public User getUserModel() {
        return userModel;
    }

    public void setUserModel(User userModel) {
        this.userModel = userModel;
        updateView();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void updateView() {
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
