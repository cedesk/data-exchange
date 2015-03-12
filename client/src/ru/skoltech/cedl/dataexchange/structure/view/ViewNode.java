package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.InstrumentModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewNode extends TreeItem<String> {

    final Node sysIcon = new ImageView(new Image(this.getClass().getResourceAsStream("../../../../../../spacecraft.png")));

    final Node subsysIcon = new ImageView(new Image(getClass().getResourceAsStream("../../../../../../subsystem.png")));

    final Node elementIcon = new ImageView(new Image(getClass().getResourceAsStream("../../../../../../element.png")));

    public ViewNode(SystemModel system) {
        super(system.getName());
        super.setGraphic(sysIcon);
    }

    public ViewNode(SubSystemModel subSystemModel) {
        super(subSystemModel.getName());
        super.setGraphic(subsysIcon);
    }

    public ViewNode(ElementModel elementModel){
        super(elementModel.getName());
        super.setGraphic(elementIcon);
    }

    public ViewNode(InstrumentModel instrumentModel){
        super(instrumentModel.getName());
    }

}
