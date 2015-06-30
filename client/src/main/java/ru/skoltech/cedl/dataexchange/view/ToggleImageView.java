package ru.skoltech.cedl.dataexchange.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by D.Knoll on 29.06.2015.
 */
public class ToggleImageView extends ImageView {

    private ObjectProperty<Image> activeImage = new ObjectPropertyBase<Image>() {
        @Override
        public Object getBean() {
            return ToggleImageView.this;
        }

        @Override
        public String getName() {
            return "activeImage";
        }
    };
    private ObjectProperty<Image> inactiveImage = new ObjectPropertyBase<Image>() {
        @Override
        public Object getBean() {
            return ToggleImageView.this;
        }

        @Override
        public String getName() {
            return "inactiveImage";
        }
    };

    private BooleanProperty activeState = new SimpleBooleanProperty(false);

    public ToggleImageView() {
        super();
        initialize();
    }

    public ToggleImageView(String inactiveUrl, String activeUrl) {
        this(new Image(inactiveUrl), new Image(activeUrl));
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
        return activeImage.get();
    }

    public void setActiveImage(Image activeImage) {
        this.activeImage.set(activeImage);
    }

    public final ObjectProperty<Image> activeImageProperty() {
        return activeImage;
    }

    public Image getInactiveImage() {
        return inactiveImage.get();
    }

    public ObjectProperty<Image> inactiveImageProperty() {
        return inactiveImage;
    }

    public void setInactiveImage(Image inactiveImage) {
        this.inactiveImage.set(inactiveImage);
    }
}
