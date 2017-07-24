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

import java.net.URL;

/**
 * URL constants define paths to control <i>*.fxml</i> files.
 *
 * Created by Nikolay Groshkov on 07-Jul-17.
 */
public class Controls {

    public static final URL PARAMETER_EDITOR_CONTROL = Controls.class.getResource("parameter_editor.fxml");
    public static final URL PARAMETER_SELECTOR_CONTROL = Controls.class.getResource("parameter_selector.fxml");
    public static final URL EXTERNAL_MODEL_CONTROL = Controls.class.getResource("external_model_view.fxml");
    public static final URL EXTERNAL_MODELS_EDITOR_CONTROL = Controls.class.getResource("external_models_editor.fxml");
    public static final URL REFERENCE_SELECTOR_CONTROL = Controls.class.getResource("reference_selector.fxml");
    public static final URL CALCULATION_EDITOR_CONTROL = Controls.class.getResource("calculation_editor.fxml");
    public static final URL CALCULATION_ARGUMENT_EDITOR_CONTROL = Controls.class.getResource("calculation_argument_editor.fxml");

}
