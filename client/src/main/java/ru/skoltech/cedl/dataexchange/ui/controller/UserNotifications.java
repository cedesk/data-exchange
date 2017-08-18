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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.util.function.Consumer;

/**
 * Created by D.Knoll on 10.07.2015.
 */
public class UserNotifications {

    public static final Image FLASH_ICON = new Image("/icons/flash-orange.png");

    public static void showNotification(Window window, String title, String text) {
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create();
            notifications.owner(window).hideAfter(Duration.seconds(3));
            notifications.title(title).text(text);
            notifications.show();
        });
    }

    public static void showActionableNotification(Window window, String title, String text, String buttonText, Consumer<ActionEvent> onClick, boolean autohide) {
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create().owner(window);
            if (autohide) {
                notifications.hideAfter(Duration.seconds(6));
            }
            notifications.action(new Action(buttonText, ae ->
                    onClick.accept(null)
            ));
            notifications.position(Pos.BOTTOM_LEFT).graphic(new ImageView(FLASH_ICON));
            notifications.title(title).text(text);
            notifications.show();
        });
    }
}
