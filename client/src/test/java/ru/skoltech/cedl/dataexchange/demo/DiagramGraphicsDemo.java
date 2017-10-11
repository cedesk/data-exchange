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

package ru.skoltech.cedl.dataexchange.demo;

/**
 * Created by d.knoll on 14.11.2016.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.ui.control.DependencyDiagramView;

public class DiagramGraphicsDemo extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        DependencyDiagramView dependencyDiagramView = new DependencyDiagramView();
        dependencyDiagramView.setMinWidth(600);
        dependencyDiagramView.setMinHeight(400);

        Scene scene = new Scene(dependencyDiagramView);
        stage.setScene(scene);
        stage.setTitle("Diagram Viewer Test");
        stage.show();

        dependencyDiagramView.initialize(null, null);
        dependencyDiagramView.addElement("Element 1");
        dependencyDiagramView.addElement("Element 2");
        dependencyDiagramView.addConnection("Element 1", "Element 2", "param. A,\nparam. Z", 2);

        dependencyDiagramView.addElement("Element 3");
        dependencyDiagramView.addConnection("Element 1", "Element 3", "parameter B", 1);
        dependencyDiagramView.addConnection("Element 3", "Element 2", "parameter C", 1);

        dependencyDiagramView.addElement("Element 4");
        dependencyDiagramView.addConnection("Element 3", "Element 4", "param. Y", 1);
        dependencyDiagramView.addConnection("Element 4", "Element 2", "param. X", 1);

    }

}