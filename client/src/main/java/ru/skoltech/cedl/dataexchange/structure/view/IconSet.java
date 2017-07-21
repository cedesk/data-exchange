/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.image.Image;

/**
 * Created by D.Knoll on 05.08.2015.
 */
public interface IconSet {

    Image APP_ICON = new Image("/icons/app-icon.png");

    Image getIcon(IconType iconType);

    class Elements implements IconSet {
        private final static Image SYS_ICON = new Image("/icons/element_l1.png");
        private final static Image SUBSYS_ICON = new Image("/icons/element_l2.png");
        private final static Image ELEMENT_ICON = new Image("/icons/element_l3.png");
        private final static Image INSTRUMENT_ICON = new Image("/icons/element_l4.png");

        @Override
        public Image getIcon(IconType iconType) {
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
                    return null; // TODO:
            }
        }
    }

    class Nodes implements IconSet {
        private final static Image SYS_ICON = new Image("/icons/bw/node_l1.png");
        private final static Image SUBSYS_ICON = new Image("/icons/bw/node_l2.png");
        private final static Image ELEMENT_ICON = new Image("/icons/bw/node_l3.png");
        private final static Image INSTRUMENT_ICON = new Image("/icons/bw/node_l4.png");

        @Override
        public Image getIcon(IconType iconType) {
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
                    return null; // TODO:
            }
        }
    }
}
