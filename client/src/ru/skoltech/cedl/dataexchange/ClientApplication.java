package ru.skoltech.cedl.dataexchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.controller.MainController;
import ru.skoltech.cedl.dataexchange.view.Views;

public class ClientApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.applicationView);
        Parent root = loader.load();
        MainController mainController = loader.getController();

        primaryStage.setTitle("Concurrent Engineering Data Exchange Skoltech");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        mainController.setup();
    }

}