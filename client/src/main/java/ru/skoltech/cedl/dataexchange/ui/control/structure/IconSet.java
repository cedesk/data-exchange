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

package ru.skoltech.cedl.dataexchange.ui.control.structure;

import javafx.scene.image.Image;

import java.util.Objects;

/**
 * Created by D.Knoll on 05.08.2015.
 */
public interface IconSet {

    Image APP_ICON = new Image("/icons/app-icon.png");

    Image getIcon(IconType iconType);

    class Nodes implements IconSet {
        private final static Image SYS_ICON = new Image("/icons/bw/node_l1.png");
        private final static Image SUBSYS_ICON = new Image("/icons/bw/node_l2.png");
        private final static Image ELEMENT_ICON = new Image("/icons/bw/node_l3.png");
        private final static Image INSTRUMENT_ICON = new Image("/icons/bw/node_l4.png");

        @Override
        public Image getIcon(IconType iconType) {
            Objects.requireNonNull(iconType);
            switch (iconType) {
                case SYSTEM:
                    return SYS_ICON;
                case SUBSYSTEM:
                    return SUBSYS_ICON;
                case ELEMENT:
                    return ELEMENT_ICON;
                case INSTRUMENT:
                    return INSTRUMENT_ICON;
                default:
                    return null; // never happens
            }
        }
    }
}
