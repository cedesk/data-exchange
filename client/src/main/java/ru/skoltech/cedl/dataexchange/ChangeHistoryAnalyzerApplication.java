/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;


import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

/**
 * Created by d.knoll on 29.12.2016.
 */
public class ChangeHistoryAnalyzerApplication extends ContextAwareApplication {

    private static Logger logger = Logger.getLogger(ChangeHistoryAnalyzerApplication.class);

    public static void main(String[] args) {
        contextInit();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadContext();
        loadLastProject();

        GuiService guiService = context.getBean(GuiService.class);
        ViewBuilder changeHistoryAnalysisViewBuilder = guiService.createViewBuilder("Parameter Change Analysis", Views.CHANGE_HISTORY_ANALYSIS_VIEW);
        changeHistoryAnalysisViewBuilder.primaryStage(primaryStage);
        changeHistoryAnalysisViewBuilder.show();
    }

}