package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class SubSystem extends ModelNode {

    public SubSystem(String name) {
        super(name);
        super.image =
                new ImageView(new Image(getClass().getResourceAsStream("../../../../../../subsystem.png")));
    }

}
