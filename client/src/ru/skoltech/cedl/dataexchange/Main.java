package ru.skoltech.cedl.dataexchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("study-editing.fxml"));
        primaryStage.setTitle("Concurrent Engineering Data Exchange Skoltech");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();


    }

}