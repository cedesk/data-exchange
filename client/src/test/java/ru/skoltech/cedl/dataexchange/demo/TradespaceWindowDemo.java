package ru.skoltech.cedl.dataexchange.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.controller.TradespaceController;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.tradespace.TradespaceFactory;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.net.URL;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceWindowDemo extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = TradespaceWindowDemo.class.getResource("/GPUdataset_2015.csv");
        File file = new File(url.getFile());

        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.buildFromCSV(file);
        System.out.println(multitemporalTradespace.toString());

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.TRADESPACE_WINDOW);
        Parent root = loader.load();
        TradespaceController controller = (TradespaceController) loader.getController();
        controller.setModel(multitemporalTradespace);

        primaryStage.setTitle("Tradespace Window Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();

    }
/*
    private static MultitemporalTradespace buildTradespace() {
        MultitemporalTradespace multitemporalTradespace = new MultitemporalTradespace();
        List<Epoch> epoches = Epoch.buildEpochs(2017, 2020, 2025, 2030);
        multitemporalTradespace.setEpochs(epoches);

        List<FigureOfMeritDefinition> definitions = buildFigureOfMeritDefinitions();
        multitemporalTradespace.setDefinitions(definitions);

        List<DesignPoint> points = buildDesignPoints(epoches, definitions);
        multitemporalTradespace.setDesignPoints(points);

        return multitemporalTradespace;
    }

    private static List<DesignPoint> buildDesignPoints(List<Epoch> epoches, List<FigureOfMeritDefinition> definitions) {
        List<DesignPoint> points = new LinkedList<>();
        Epoch currentEpoch = epoches.get(0);

        // 1 design point
        List<FigureOfMeritValue> values = new ArrayList<>(definitions.size());
        values.add(new FigureOfMeritValue(definitions.get(0), 123d));
        values.add(new FigureOfMeritValue(definitions.get(1), 47.4));
        values.add(new FigureOfMeritValue(definitions.get(2), 40.0));
        values.add(new FigureOfMeritValue(definitions.get(3), 240000d));
        points.add(new DesignPoint(currentEpoch, values));

        return points;
    }

    private static List<FigureOfMeritDefinition> buildFigureOfMeritDefinitions() {
        List<FigureOfMeritDefinition> foms = new ArrayList<>();
        foms.add(new FigureOfMeritDefinition("cost", "USD"));
        foms.add(new FigureOfMeritDefinition("energy density", "Wh/kg"));
        foms.add(new FigureOfMeritDefinition("depth of discharge", "percent"));
        foms.add(new FigureOfMeritDefinition("duty cycles", "number"));
        return foms;
    }
*/
}
