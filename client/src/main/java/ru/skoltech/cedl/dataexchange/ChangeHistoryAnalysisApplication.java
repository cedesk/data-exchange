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

import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class ChangeHistoryAnalysisApplication extends ContextAwareApplication {

    private static Logger logger = Logger.getLogger(ChangeHistoryAnalysisApplication.class);

    public static void main(String[] args) {
        contextInit();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadContext();
        loadLastProject();
        String projectName = this.project.getProjectName();

        GuiService guiService = context.getBean(GuiService.class);
        ViewBuilder changeHistoryAnalysis = guiService.createViewBuilder("CEDESK Change History Analysis - " + projectName, Views.CHANGE_HISTORY_ANALYSIS_VIEW);
        changeHistoryAnalysis.primaryStage(primaryStage);
        changeHistoryAnalysis.show();
    }

}
