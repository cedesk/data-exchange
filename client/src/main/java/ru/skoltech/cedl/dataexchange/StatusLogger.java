package ru.skoltech.cedl.dataexchange;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class StatusLogger {

    private static StatusLogger instance = new StatusLogger();

    private StringProperty lastMessage = new SimpleStringProperty();

    private StatusLogger() {
    }

    public static StatusLogger getInstance() {
        return instance;
    }

    public String getLastMessage() {
        return lastMessage.get();
    }

    public StringProperty lastMessageProperty() {
        return lastMessage;
    }

    public void log(String msg, boolean error) {
        if (error) {
            System.err.println(msg);
        } else {
            System.out.println(msg);
        }
        lastMessage.setValue(msg);
    }

    public void log(String msg) {
        log(msg, false);
    }
}
