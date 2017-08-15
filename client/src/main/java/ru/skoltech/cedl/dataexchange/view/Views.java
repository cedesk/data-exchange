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

package ru.skoltech.cedl.dataexchange.view;

import java.net.URL;

/**
 * URL constants define paths to view <i>*.fxml</i> files.
 *
 * Created by D.Knoll on 20.03.2015.
 */
public class Views {

    public static final URL MAIN_WINDOW = Views.class.getResource("main-window.fxml");
    public static final URL MODEL_EDITING_VIEW = Views.class.getResource("model-editing.fxml");
    public static final URL MODEL_DIFF_WINDOW = Views.class.getResource("model-diff-window.fxml");
    public static final URL MODEL_CONSISTENCY_WINDOW = Views.class.getResource("model-consistency-window.fxml");
    public static final URL DSM_WINDOW = Views.class.getResource("dsm-window.fxml");
    public static final URL DEPENDENCY_WINDOW = Views.class.getResource("dependency-window.fxml");
    public static final URL USER_MANAGEMENT_WINDOW = Views.class.getResource("user-management-window.fxml");
    public static final URL USER_EDITING_WINDOW = Views.class.getResource("user-editing-window.fxml");
    public static final URL USER_ROLE_MANAGEMENT_WINDOW = Views.class.getResource("user-role-management-window.fxml");
    public static final URL UNIT_EDITING_WINDOW = Views.class.getResource("unit-editing-window.fxml");
    public static final URL UNIT_ADD_WINDOW = Views.class.getResource("add-unit-dialog.fxml");
    public static final URL REVISION_HISTORY_WINDOW = Views.class.getResource("revision-history-window.fxml");
    public static final URL PROJECT_SETTINGS_WINDOW = Views.class.getResource("project-settings-window.fxml");
    public static final URL REPOSITORY_SETTINGS_WINDOW = Views.class.getResource("repository-settings-window.fxml");
    public static final URL ABOUT_WINDOW = Views.class.getResource("about-window.fxml");
    public static final URL GUIDE_WINDOW = Views.class.getResource("guide-window.fxml");

}
