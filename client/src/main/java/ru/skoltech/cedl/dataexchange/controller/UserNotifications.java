package ru.skoltech.cedl.dataexchange.controller;

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

    public static void showActionableNotification(Window window, String title, String text, String buttonText, Consumer<ActionEvent> onClick) {
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create().owner(window);
            notifications.hideAfter(Duration.seconds(5));
            notifications.action(new Action(buttonText, ae ->
                    onClick.accept(null)
            ));
            notifications.position(Pos.BOTTOM_LEFT).graphic(new ImageView(FLASH_ICON));
            notifications.title(title).text(text);
            notifications.show();
        });
    }
}
