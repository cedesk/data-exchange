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

    public Collection<String> getLastMessages() {
        return lineBuffer;
    }

    public StringProperty lastMessageProperty() {
        return lastMessage;
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
