package ru.skoltech.cedl.dataexchange.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.TradespaceView;
import ru.skoltech.cedl.dataexchange.tradespace.FigureOfMeritChartDefinition;
import ru.skoltech.cedl.dataexchange.tradespace.MultitemporalTradespace;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceController implements Initializable {

    private static final Logger logger = Logger.getLogger(TradespaceController.class);

    @FXML
    private TradespaceView tradespaceView;

    private MultitemporalTradespace model;

    public TradespaceController() {
    }

    public MultitemporalTradespace getModel() {
        return model;
    }

    public void setModel(MultitemporalTradespace model) {
        this.model = model;
        FigureOfMeritChartDefinition chartDef = new FigureOfMeritChartDefinition(model.getDefinitions().get(0), model.getDefinitions().get(1));
        tradespaceView.setModel(model, chartDef);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void refreshView(ActionEvent actionEvent) {

    }

    public void saveDiagram(ActionEvent actionEvent) {
        String xAxisName = tradespaceView.getChartDefinition().getAxis1().getName();
        String yAxisName = tradespaceView.getChartDefinition().getAxis2().getName();

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName("FigureOfMeritChart_" + xAxisName + "--" + yAxisName + "_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Chart");
        Window window = tradespaceView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = tradespaceView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving chart to file", e);
            }
        }
    }
}
