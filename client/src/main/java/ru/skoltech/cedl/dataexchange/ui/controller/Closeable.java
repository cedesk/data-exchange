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

import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * Mark a JavaFX Controller which can react on close event of parent {@link Window}.
 * <p>
 * Created by Nikolay Groshkov on 13-Aug-17.
 */
public interface Closeable {

    /**
     * Reaction on parent {@link Window} close.
     *
     * @param stage       {@link Stage} which is going to be closed
     * @param windowEvent window close event
     */
    void close(Stage stage, WindowEvent windowEvent);
}
