/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.demo;

/**
 * Created by d.knoll on 14.11.2016.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.control.DiagramView;

public class DiagramGraphicsDemo extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        DiagramView diagramView = new DiagramView();
        diagramView.setMinWidth(600);
        diagramView.setMinHeight(400);

        Scene scene = new Scene(diagramView);
        stage.setScene(scene);
        stage.setTitle("Diagram Viewer Test");
        stage.show();

        diagramView.initialize(null, null);
        diagramView.addElement("Element 1");
        diagramView.addElement("Element 2");
        diagramView.addConnection("Element 1", "Element 2", "param. A,\nparam. Z", 2);

        diagramView.addElement("Element 3");
        diagramView.addConnection("Element 1", "Element 3", "parameter B", 1);
        diagramView.addConnection("Element 3", "Element 2", "parameter C", 1);

        diagramView.addElement("Element 4");
        diagramView.addConnection("Element 3", "Element 4", "param. Y", 1);
        diagramView.addConnection("Element 4", "Element 2", "param. X", 1);

    }

}