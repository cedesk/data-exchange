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
import ru.skoltech.cedl.dataexchange.ui.control.DsmView;

public class DsmViewDemo extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        DsmView dsmView = new DsmView();
        dsmView.setMinWidth(400);
        dsmView.setMinHeight(400);

        Scene scene = new Scene(dsmView);
        stage.setScene(scene);
        stage.setTitle("Diagram Viewer Test");
        stage.show();

        dsmView.initialize(null, null);

        dsmView.addElement("Car Design");
        dsmView.addElement("Chassis");
        dsmView.addConnection("Car Design", "Chassis", "maximum speeed", 1);

        dsmView.addElement("Accomodation");
        dsmView.addElement("Navigation");
        dsmView.addConnection("Accomodation", "Navigation", "passengers", 1);

        dsmView.addElement("Traction");
        dsmView.addConnection("Car Design", "Traction", "maximum mass,\nmaximum speed", 2);
        dsmView.addConnection("Traction", "Chassis", "engine Volume", 1);

    }

}