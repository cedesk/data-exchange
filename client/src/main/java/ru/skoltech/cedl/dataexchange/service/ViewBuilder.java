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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ui.control.structure.IconSet;
import ru.skoltech.cedl.dataexchange.ui.controller.Applicable;
import ru.skoltech.cedl.dataexchange.ui.controller.Closeable;
import ru.skoltech.cedl.dataexchange.ui.controller.Displayable;
import ru.skoltech.cedl.dataexchange.ui.controller.FXMLLoaderFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A special builder class instances of which allow to construct JavaFX views.
 * After adjusting some basic parameters the methods {@link ViewBuilder#show(Object...)} or {@link ViewBuilder#showAndWait(Object...)}
 * must be used for launching a new view.
 *
 * @param <T> type of JavaFX controller.
 *            <p>
 *            Created by Nikolay Groshkov on 12-Aug-17.
 */
public class ViewBuilder<T> {

    private static final Logger logger = Logger.getLogger(ViewBuilder.class);

    private FXMLLoaderFactory fxmlLoaderFactory;
    private Locale locale;
    private String title;
    private URL location;
    private Stage primaryStage;
    private Window ownerWindow;
    private Modality modality = Modality.NONE;
    private Double x;
    private Double y;
    private boolean resizable = true;
    private StageStyle initStyle = StageStyle.DECORATED;

    private T controller;
    private EventHandler<WindowEvent> displayEventHandler;
    private EventHandler<WindowEvent> closeEventHandler;
    private EventHandler<Event> applyEventHandler;

    /**
     * Create an instance of {@link ViewBuilder}.
     *
     * @param fxmlLoaderFactory loader factory of <i>*.fxml</i> files
     * @param title             title of feature view
     * @param location          location of <i>*.fxml</i> file of feature view
     */
    public ViewBuilder(FXMLLoaderFactory fxmlLoaderFactory, Locale locale, String title, URL location) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.locale = locale;
        this.title = title;
        this.location = location;
    }

    /**
     * Setup an event handler which is performed when a feature view will produce some changes.
     *
     * @param applyEventHandler apply event handler of view
     */
    public void applyEventHandler(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    /**
     * Setup an event handler which is performed when a feature view will be closed.
     *
     * @param closeEventHandler close event handler of view
     */
    public void closeEventHandler(EventHandler<WindowEvent> closeEventHandler) {
        this.closeEventHandler = closeEventHandler;
    }

    /**
     * Setup an event handler which is performed when a feature view will be displayed.
     *
     * @param displayEventHandler display event handler of view
     */
    public void displayEventHandler(EventHandler<WindowEvent> displayEventHandler) {
        this.displayEventHandler = displayEventHandler;
    }

    /**
     * Setup a modality of a feature view.
     *
     * @param modality modality of view
     */
    public void modality(Modality modality) {
        this.modality = modality;
    }

    /**
     * Setup an owner {@link Window} of a feature view.
     *
     * @param ownerWindow owner {@link Window}
     */
    public void ownerWindow(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    /**
     * Start a feature view under primary stage.
     * Can be used only for base application views.
     *
     * @param primaryStage primary stage
     */
    public void primaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Setup a init state style of a feature view.
     *
     * @param initStyle {@link StageStyle} parameter of view
     */
    public void initStyle(StageStyle initStyle) {
        this.initStyle = initStyle;
    }

    /**
     * Setup a resizable parameter of a feature view
     * (<i>true</i> by default).
     *
     * @param resizable resizable parameter of view
     */
    public void resizable(boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * Setup a horizontal location on the screen of a feature view.
     *
     * @param x horizontal location of view
     */
    public void x(double x) {
        this.x = x;
    }

    /**
     * Setup a vertical location on the screen of a feature view.
     *
     * @param y vertical location of view
     */
    public void y(double y) {
        this.y = y;
    }

    /**
     * Setup both horizontal and vertical location on the screen of a feature view.
     *
     * @param x horizontal location of view
     * @param y vertical location of view
     */
    public void xy(double x, double y) {
        x(x);
        y(y);
    }

    /**
     * Display a view on current adjustments.
     * This must be a final operation of the builder.
     *
     * @param args arguments for JavaFX controller constructor of a view
     * @return an instance of JavaFX controller
     */
    public T show(Object... args) {
        this.createStage(args).show();
        return controller;
    }

    /**
     * Display a view on current adjustments and waits for it to be hidden (closed) before returning.
     *
     * @param args arguments for JavaFX controller constructor of a view
     * @return an instance of JavaFX controller
     */
    public T showAndWait(Object... args) {
        this.createStage(args).showAndWait();
        return controller;
    }

    /**
     * Create a stage instance based on current adjustments.
     *
     * @param args arguments for JavaFX controller constructor of a view
     * @return an instance of created {@link Stage}
     */
    public Stage createStage(Object... args) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(location, args);
            loader.setResources(ResourceBundle.getBundle("i18n.MessagesBundle", locale));

            Parent root = loader.load();
            controller = loader.getController();
            if (displayEventHandler == null) {
                displayEventHandler = event -> ((Displayable) controller).display((Stage) event.getSource(), event);
            }
            if (closeEventHandler == null) {
                closeEventHandler = event -> ((Closeable) controller).close((Stage) event.getSource(), event);
            }

            Stage stage = primaryStage != null ? primaryStage : new Stage();
            if (x != null) {
                stage.setX(x);
            }
            if (y != null) {
                stage.setY(y);
            }
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.getIcons().add(IconSet.APP_ICON);
            stage.setResizable(resizable);
            stage.initStyle(initStyle);

            if (primaryStage == null) {
                stage.initModality(modality);
            }

            if (ownerWindow != null) {
                stage.initOwner(ownerWindow);
                stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if(event.getCode() == KeyCode.ESCAPE){
                        stage.close();
                    }
                });
            }

            if (controller instanceof Displayable) {
                stage.setOnShown(displayEventHandler);
            }
            if (controller instanceof Closeable) {
                stage.setOnCloseRequest(closeEventHandler);
            }

            if (applyEventHandler != null) {
                if (controller instanceof Applicable) {
                    ((Applicable) controller).setOnApply(applyEventHandler);
                } else {
                    logger.warn("Cannot setup applyEventHandler because controller " +
                            controller.getClass().getCanonicalName() +
                            " does not implement " + Applicable.class.getCanonicalName() + " interface");
                }
            }

            return stage;
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException("Unable to load view: " + location, e);
        }
    }

}
