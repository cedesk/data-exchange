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

package ru.skoltech.cedl.dataexchange.ui.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by D.Knoll on 29.06.2015.
 */
public class ToggleImageView extends ImageView {

    private Image activeImage;

    private Image inactiveImage;

    private BooleanProperty activeState = new SimpleBooleanProperty(false);

    public ToggleImageView() {
        super();
        initialize();
    }

    public ToggleImageView(Image inactiveImage, Image activeImage) {
        super(inactiveImage);
        setInactiveImage(inactiveImage);
        setActiveImage(activeImage);
        initialize();
    }

    private void initialize() {
        activeState.addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        toggle(newValue);
                    }
                }
        );
    }

    public boolean getActiveState() {
        return activeState.get();
    }

    public void setActiveState(boolean activeState) {
        this.activeState.set(activeState);
    }

    public BooleanProperty activeStateProperty() {
        return activeState;
    }

    private void toggle(boolean active) {
        if (active) {
            super.setImage(getActiveImage());
        } else {
            super.setImage(getInactiveImage());
        }
    }

    public Image getActiveImage() {
        return activeImage;
    }

    public void setActiveImage(Image activeImage) {
        this.activeImage = activeImage;
    }

    public Image getInactiveImage() {
        return inactiveImage;
    }

    public void setInactiveImage(Image inactiveImage) {
        this.inactiveImage = inactiveImage;
    }
}
