package ru.skoltech.cedl.dataexchange.controller;

import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * Created by D.Knoll on 10.07.2015.
 */
public class UserNotifications {

    public static void showNotification(Window window, String title, String text) {
        Notifications notifications = Notifications.create();
        notifications.owner(window).hideAfter(Duration.seconds(5));
        notifications.title(title).text(text);
        notifications.showInformation();
    }
}
