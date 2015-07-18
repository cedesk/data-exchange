package ru.skoltech.cedl.dataexchange.view;

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
