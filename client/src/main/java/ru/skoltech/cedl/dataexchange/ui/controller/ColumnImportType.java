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

import javafx.util.StringConverter;

/**
 * Enum which defines various types of entities for importing into tradespace.
 * <p>
 * Created by Nikolay Groshkov on 01-Dec-17.
 */
public enum ColumnImportType {
    EMPTY(""),
    DESCRIPTION("Description"),
    FIGURE_OF_MERIT("Figure of merit"),
    EPOCH("Epoch");

    String type;

    ColumnImportType(String type) {
        this.type = type;
    }

    public static class ColumnImportTypeStringConverter extends StringConverter<ColumnImportType> {

        @Override
        public String toString(ColumnImportType columnImportType) {
            return columnImportType != null ? columnImportType.type : null;
        }

        @Override
        public ColumnImportType fromString(String string) {
            return ColumnImportType.valueOf(string);
        }
    }
}