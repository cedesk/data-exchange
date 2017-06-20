package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.RadioButton;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.DiagramView;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 02.11.2015.
 */
public class DependencyController implements Initializable {

    private static final Logger logger = Logger.getLogger(DependencyController.class);

    @FXML
    private RadioButton sortOrderDefault;

    @FXML
    private RadioButton sortOrderAlphabetical;

    @FXML
    private RadioButton sortOrderPriority;

    @FXML
    private DiagramView diagramView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> refreshView(null));
    }

    public void refreshView(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();
        SystemModel systemModel = project.getSystemModel();
        ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
        DependencyModel dependencyModel = parameterLinkRegistry.getDependencyModel(systemModel);

        if (sortOrderDefault.isSelected()) {
            HashMap<String, Integer> originalPositions = new HashMap<>();
            originalPositions.put(systemModel.getName(), 0);
            List<SubSystemModel> subNodes = systemModel.getSubNodes();
            for (int i = 0; i < subNodes.size(); i++) {
                originalPositions.put(subNodes.get(i).getName(), i + 1);
            }
            dependencyModel.elementStream()
                    .forEach(element -> element.setPosition(originalPositions.get(element.getName())));
        } else if (sortOrderPriority.isSelected()) {
            final int[] position = {0};
            dependencyModel.elementStream()
                    .sorted(dependencyModel.priorityComparator)
                    .forEach(element -> element.setPosition(position[0]++));
        } else {
            final int[] position = {0};
            dependencyModel.elementStream()
                    .sorted(Comparator.comparing(DependencyModel.Element::getName))
                    .forEach(element -> element.setPosition(position[0]++));
        }
        diagramView.setModel(dependencyModel);

    }

    public void saveDiagram(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        //fc.setInitialDirectory(new File("res/maps"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName(ProjectContext.getInstance().getProject().getProjectName() + "_NSquare_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Diagram");
        Window window = diagramView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = diagramView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving diagram to file", e);
            }
        }
    }

}
