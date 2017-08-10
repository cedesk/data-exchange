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

package ru.skoltech.cedl.dataexchange.services;

import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Window;
import ru.skoltech.cedl.dataexchange.services.impl.GuiServiceImpl;

import java.net.URL;

/**
 * Operations with GUI
 *
 * Created by Nikolay Groshkov on 09-Aug-17.
 */
public interface GuiService {

    /**
     * Defines state of the stage after start.
     * Uses for open view utility methods.
     */
    public enum StageStartAction {

        /**
         * A stage just show a window after start.
         */
        SHOW,

        /**
         * A stage show a window and wait till its close.
         */
        SHOW_AND_WAIT
    }

    /**
     * Open window with specified parameters.
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     */
    void openView(String title, URL location, Window ownerWindow);

    /**
     * Open window with specified parameters.
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     * @param stageStartAction state of the stage after start
     */
    void openView(String title, URL location, Window ownerWindow, StageStartAction stageStartAction);

    /**
     * Open window with specified parameters.
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     * @param args arguments of controllers constructor
     */
    void openView(String title, URL location, Window ownerWindow, Object... args);

    /**
     * Open window with specified parameters.
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     * @param modality modality of the window
     */
    void openView(String title, URL location, Window ownerWindow, Modality modality);

    /**
     * Open window with specified parameters.
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     * @param modality modality of the window
     * @param stageStartAction state of the stage after start
     */
    void openView(String title, URL location, Window ownerWindow, Modality modality, GuiServiceImpl.StageStartAction stageStartAction);

    /**
     * Open window with specified parameters.
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     * @param modality modality of the window
     * @param args arguments of controllers constructor
     */
    void openView(String title, URL location, Window ownerWindow, Modality modality, Object... args);

    /**
     * Open window with specified parameters
     *
     * @param title title of window
     * @param location location of <i>*.fxml</i> file
     * @param ownerWindow the owner window
     * @param modality modality of the window
     * @param stageStartAction state of the stage after start
     * @param args arguments of controllers constructor
     */
    void openView(String title, URL location, Window ownerWindow, Modality modality, StageStartAction stageStartAction, Object... args);

    /**
     * TODO: add javadoc
     *
     * @param guideView
     * @param resourceClass
     * @param filename
     */
    void loadWebView(WebView guideView, Class resourceClass, String filename);

    /**
     * TODO: add javadoc
     *
     * @param code
     */
    void copyTextToClipboard(String code);
}
