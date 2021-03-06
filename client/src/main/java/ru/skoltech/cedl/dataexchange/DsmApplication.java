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
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.init.impl.ApplicationSettingsImpl;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class DsmApplication extends ContextAwareApplication {

    private static Logger logger = Logger.getLogger(DsmApplication.class);

    public static void main(String[] args) {
        contextInit();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadContext();
        ((ApplicationSettingsImpl) applicationSettings).setRepositoryWatcherAutosync(false);
        loadLastProject();
        sortTree(project.getSystemModel());

        GuiService guiService = context.getBean(GuiService.class);
        ViewBuilder cedeskDsmViewer = guiService.createViewBuilder("CEDESK DSM Viewer", Views.DSM_VIEW);
        cedeskDsmViewer.primaryStage(primaryStage);
        cedeskDsmViewer.show();
    }

    private void sortTree(ModelNode modelNode) {
        if (modelNode instanceof CompositeModelNode) {
            List<ModelNode> modelNodes = ((CompositeModelNode) modelNode).getSubNodes();
            for (Object subnode : modelNodes) {
                sortTree((ModelNode) subnode);
            }
            modelNodes.sort(ModelNode::compareTo);
        }
    }

}
