package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

/**
 * Created by D.Knoll on 27.04.2015.
 */
public class ParameterFieldCell extends TextFieldTableCell<ParameterModel, Object> {

    private final static Image FLASH_ICON = new Image("/icons/flash-orange.png");

    public ParameterFieldCell() {
    }

    public ParameterFieldCell(StringConverter converter) {
        setConverter(converter);
    }

    @Override
    public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        setContentDisplay(ContentDisplay.RIGHT);
        setGraphicTextGap(4.0);

        if (item != null && !empty) {
            setText(item.toString());
            ParameterModel parameterModel = (ParameterModel) getTableRow().getItem();
            setEditable(parameterModel != null && parameterModel.getValueSource() == ParameterValueSource.MANUAL);
            if (parameterModel != null && parameterModel.hasServerChange()) {
                // set graphical hint
                ImageView imageView = new ImageView(FLASH_ICON);
                imageView.setFitWidth(8);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                setGraphic(imageView);
                setGraphicTextGap(8);

                // set tooltip
                Tooltip tooltip = new Tooltip();
                tooltip.setText("Newer value in repository: " + parameterModel.getServerValue().toString());
                setTooltip(tooltip);
            } else {
                setGraphic(null);
            }
        } else {
            setText(null);
            setGraphic(null);
            setEditable(false);
        }
    }
}
