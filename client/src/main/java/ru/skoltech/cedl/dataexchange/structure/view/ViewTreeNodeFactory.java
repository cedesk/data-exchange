package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.skoltech.cedl.dataexchange.structure.model.*;

/**
 * Created by D.Knoll on 29.03.2015.
 */
public class ViewTreeNodeFactory {

    private final static Image SYS_ICON = new Image("/icons/spacecraft.png");

    private final static Image SUBSYS_ICON = new Image("/icons/subsystem.png");

    private final static Image ELEMENT_ICON = new Image("/icons/element.png");

    public static ViewNode getViewTreeNode(ModelNode model) {

        ViewNode viewNode = new ViewNode(model);
        if (model instanceof SystemModel) {
            viewNode.setGraphic(new ImageView(SYS_ICON));
        } else if (model instanceof SubSystemModel) {
            viewNode.setGraphic(new ImageView(SUBSYS_ICON));
        } else if (model instanceof ElementModel) {
            viewNode.setGraphic(new ImageView(ELEMENT_ICON));
        } else if (model instanceof InstrumentModel) {
            // no icon yet
        } else {
            System.err.println("UNKNOWN model encountered: " + model.getName() + " (" + model.getClass().getName() + "");
        }
        return viewNode;
    }
}