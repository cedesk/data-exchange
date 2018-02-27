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

package ru.skoltech.cedl.dataexchange.service;

import javafx.fxml.LoadException;
import javafx.scene.Node;

import java.net.URL;

/**
 * Operations with GUI
 * <p>
 * Created by Nikolay Groshkov on 09-Aug-17.
 */
public interface GuiService {

    /**
     * Copy the text to the systems clipboard.
     *
     * @param code
     */
    void copyTextToClipboard(String code);

    /**
     * Create a new JavaFX control instance, based on some arguments.
     *
     * @param location location of <i>*.fxml</i> file of control
     * @param args     arguments for control constructor
     * @param <T>      type of control
     * @return an instance of new control
     */
    <T extends Node> T createControl(URL location, Object... args);

    /**
     * Create a new JavaFX control instance.
     *
     * @param location location of <i>*.fxml</i> file of control
     * @param <T>      type of control
     * @return an instance of new control
     */
    <T extends Node> T createControl(URL location);

    /**
     * Create a new instance of View Builder for GUI creation.
     *
     * @param title    the of the view
     * @param location location of <i>*.fxml</i> file.
     * @return instance of {@link ViewBuilder}
     */
    ViewBuilder createViewBuilder(String title, URL location);

    /**
     * Load content of external resource file.
     *
     * @param location location of the file.
     * @return content of resource
     */
    String loadResourceContent(URL location) throws LoadException;
}
