package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.FiguresOfMeritEditor;
import ru.skoltech.cedl.dataexchange.control.TradespaceView;
import ru.skoltech.cedl.dataexchange.tradespace.FigureOfMeritChartDefinition;
import ru.skoltech.cedl.dataexchange.tradespace.FigureOfMeritDefinition;
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
    private ComboBox<FigureOfMeritDefinition> xAxisCombo;

    @FXML
    private ComboBox<FigureOfMeritDefinition> yAxisCombo;

    @FXML
    private TradespaceView tradespaceView;

    @FXML
    private FiguresOfMeritEditor figuresOfMeritEditor;

    private MultitemporalTradespace model;

    public TradespaceController() {
    }

    public MultitemporalTradespace getModel() {
        return model;
    }

    public void setModel(MultitemporalTradespace model) {
        this.model = model;
        figuresOfMeritEditor.setTradespace(model);
        tradespaceView.setTradespace(model);
        updateComboBoxes();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StringConverter<FigureOfMeritDefinition> stringConverter = new StringConverter<FigureOfMeritDefinition>() {
            @Override
            public FigureOfMeritDefinition fromString(String unitStr) {
                return null;
            }

            @Override
            public String toString(FigureOfMeritDefinition figureOfMeritDefinition) {
                if (figureOfMeritDefinition == null) {
                    return null;
                }
                return figureOfMeritDefinition.getName();
            }
        };
        xAxisCombo.setConverter(stringConverter);
        xAxisCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateView();
            }
        });
        yAxisCombo.setConverter(stringConverter);
        yAxisCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateView();
            }
        });
    }

    public void refreshView(ActionEvent actionEvent) {
        updateView();
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

    private void updateComboBoxes() {
        xAxisCombo.setItems(FXCollections.observableArrayList(model.getDefinitions()));
        xAxisCombo.getSelectionModel().select(model.getDefinitions().get(0));
        yAxisCombo.setItems(FXCollections.observableArrayList(model.getDefinitions()));
        yAxisCombo.getSelectionModel().select(model.getDefinitions().get(1));
    }

    private void updateView() {
        FigureOfMeritDefinition axis1 = xAxisCombo.getValue();
        FigureOfMeritDefinition axis2 = yAxisCombo.getValue();
        FigureOfMeritChartDefinition chartDef = new FigureOfMeritChartDefinition(axis1, axis2);
        tradespaceView.setChartDefinition(chartDef);
        tradespaceView.updateView();
    }
}
