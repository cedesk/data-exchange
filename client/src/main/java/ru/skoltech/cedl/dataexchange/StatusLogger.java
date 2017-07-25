/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.collections4.BoundedCollection;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class StatusLogger {

    final static Logger logger = Logger.getLogger(StatusLogger.class);

    final static BoundedCollection<String> lineBuffer = new CircularFifoQueue<String>(10);

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

    public Collection<String> getLastMessages() {
        return lineBuffer;
    }

    public void log(String msg, boolean error) {
        lineBuffer.add(msg);
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
