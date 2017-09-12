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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

/**
 * Collect messages for display in main panel status bar.
 *
 * Created by D.Knoll on 28.03.2015.
 */
public class StatusLogger {

    /**
     * Possible types of log messages.
     */
    public enum LogType {
        INFO,
        WARN,
        ERROR
    }

    private final static Logger logger = Logger.getLogger(StatusLogger.class);

    private ObservableList<Pair<String, LogType>> messages;
    private StringProperty lastMessage = new SimpleStringProperty();
    private ObjectProperty<LogType> lastLogType = new SimpleObjectProperty<>(LogType.INFO);

    public void setSize(int size) {
        messages = FXCollections.observableArrayList(new CircularFifoQueue<>(size));
    }

    /**
     * Retrieve a collection of recently logged messages
     *
     * @return collection or recently logged messages
     */
    public ObservableList<Pair<String, LogType>> messages() {
        return messages;
    }

    /**
     * Retrieve an observable property of last logged message.
     *
     * @return property of last logged message
     */
    public StringProperty lastMessageProperty() {
        return lastMessage;
    }

    /**
     * Retrieve an observable property of last logged type.
     *
     * @return property of last logged type
     */
    public ObjectProperty<LogType> lastLogTypeProperty() {
        return lastLogType;
    }

    /**
     * Register an info message for logging.
     *
     * @param message info message to log
     */
    public void info(String message) {
        log(message, LogType.INFO);
    }

    /**
     * Register a warn message for logging.
     *
     * @param message warn message to log
     */
    public void warn(String message) {
        log(message, LogType.WARN);
    }

    /**
     * Register a error message for logging.
     *
     * @param message error message to log
     */
    public void error(String message) {
        log(message, LogType.ERROR);
    }

    private void log(String message, LogType logType) {
        if (logType == LogType.ERROR) {
            logger.error(message);
        } else if (logType == LogType.WARN){
            logger.warn(message);
        } else {
            logger.info(message);
        }
        Platform.runLater(() -> {
            messages.add(Pair.of(message, logType));
            lastMessage.setValue(message);
            lastLogType.setValue(logType);
        });
    }

}
