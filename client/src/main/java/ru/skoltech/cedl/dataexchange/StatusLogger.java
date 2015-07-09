package ru.skoltech.cedl.dataexchange;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class StatusLogger {

    final static Logger logger = Logger.getLogger(StatusLogger.class);

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
            logger.error(msg);
        } else {
            logger.info(msg);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lastMessage.setValue(msg);
            }
        });
    }

    public void log(String msg) {
        log(msg, false);
    }
}
