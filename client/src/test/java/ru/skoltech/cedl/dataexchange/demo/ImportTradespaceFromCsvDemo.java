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

package ru.skoltech.cedl.dataexchange.demo;

import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextDemo;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

/**
 * Created by Nikolay Groshkov on 28-Sep-17.
 */
public class ImportTradespaceFromCsvDemo extends AbstractApplicationContextDemo {

    @Override
    public void demo(Stage primaryStage) {
        GuiService guiService = context.getBean(GuiService.class);
        ViewBuilder viewBuilder = guiService.createViewBuilder("Import CSV into the Tradespace", Views.IMPORT_TRADESPACE_FROM_CSV_VIEW);
        viewBuilder.showAndWait();
    }
}
