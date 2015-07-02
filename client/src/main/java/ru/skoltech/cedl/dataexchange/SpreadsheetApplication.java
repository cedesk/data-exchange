package ru.skoltech.cedl.dataexchange;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.controller.MainController;
import ru.skoltech.cedl.dataexchange.controller.SpreadsheetController;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;

public class SpreadsheetApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.SPREADSHEET_VIEW);
        Parent root = loader.load();
        SpreadsheetController spreadsheetController = loader.getController();

        primaryStage.setTitle("CEDESK Spreadsheet Viewer");
        primaryStage.setScene(new Scene(root, 800, 480));
        primaryStage.getIcons().add(new Image("/icons/app-icon.png"));
        primaryStage.show();
        primaryStage.setOnCloseRequest(we -> spreadsheetController.close());
    }
}