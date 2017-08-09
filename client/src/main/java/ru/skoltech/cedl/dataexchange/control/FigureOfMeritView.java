package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.tradespace.TradespaceModelBridge;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class FigureOfMeritView extends FlowPane implements Initializable {

    private static final Logger logger = Logger.getLogger(FigureOfMeritView.class);

    private FigureOfMeritDefinition figureOfMeritDefinition;

    @FXML
    private TextField figureOfMeritNameText;

    @FXML
    private TextField parameterLinkText;

    public FigureOfMeritView() {
        super();
        figureOfMeritDefinition = new FigureOfMeritDefinition("figure of merit", "unit");
    }

    public FigureOfMeritView(FigureOfMeritDefinition figureOfMeritDefinition) {
        this.figureOfMeritDefinition = figureOfMeritDefinition;
        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("figure_of_merit_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public FigureOfMeritDefinition getFigureOfMeritDefinition() {
        return figureOfMeritDefinition;
    }

    public void chooseParameter(ActionEvent actionEvent) {

        Collection<ParameterModel> parameters = TradespaceModelBridge.getModelOutputParameters();
        Dialog<ParameterModel> dialog = new ParameterSelector(parameters, null);

        Optional<ParameterModel> parameterChoice = dialog.showAndWait();
        if (parameterChoice.isPresent()) {
            ParameterModel parameterModel = parameterChoice.get();
            figureOfMeritDefinition.setParameterModelLink(parameterModel.getUuid());
            updateView();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateView();
    }

    private void updateView() {
        if (figureOfMeritDefinition != null) {
            figureOfMeritNameText.setText(figureOfMeritDefinition.getName());

            if (figureOfMeritDefinition.getParameterModelLink() != null) {
                String parameterName = TradespaceModelBridge.getParameterName(figureOfMeritDefinition.getParameterModelLink());
                parameterLinkText.setText(parameterName);
            }
        }
    }

}
