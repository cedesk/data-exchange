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

package ru.skoltech.cedl.dataexchange.control;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Nikolay Groshkov on 04-Aug-17.
 */
public class ErrorAlert extends Alert {

    public ErrorAlert(Throwable e) {
        super(Alert.AlertType.ERROR);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        Text messageText1Text = new Text("Error occurs during the application startup.\n");
        Text messageText2Text = new Text("Ask for administrators support.\n\n");
        TextArea stackTraceTextArea = new TextArea(stackTrace);
        stackTraceTextArea.setEditable(false);
        stackTraceTextArea.setWrapText(true);
        stackTraceTextArea.setPrefWidth(900);
        stackTraceTextArea.setPrefHeight(300);

        this.setTitle("Startup error");
        this.getDialogPane().setContent(new TextFlow(messageText1Text, messageText2Text, stackTraceTextArea));
        this.getDialogPane().setMinWidth(900);

    }
}
