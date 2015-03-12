package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.io.InputStream;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewNode extends TreeItem<ModelNode> {

    final Node sysIcon = new ImageView(new Image(Utils.getResourceAsStream("spacecraft.png")));

    final Node subsysIcon = new ImageView(new Image(Utils.getResourceAsStream("subsystem.png")));

    final Node elementIcon = new ImageView(new Image(Utils.getResourceAsStream("element.png")));

    public ViewNode(SystemModel system) {
        super(system);
        super.setGraphic(sysIcon);
    }

    public ViewNode(SubSystemModel subSystemModel) {
        super(subSystemModel);
        super.setGraphic(subsysIcon);
    }

    public ViewNode(ElementModel elementModel){
        super(elementModel);
        super.setGraphic(elementIcon);
    }

    public ViewNode(InstrumentModel instrumentModel){
        super(instrumentModel);
    }

}
