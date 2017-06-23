package ru.skoltech.cedl.dataexchange.demo;

import ru.skoltech.cedl.dataexchange.DependencyGraphTest;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.tradespace.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceDemo {

    public static void main(String... args) {

        URL url = TradespaceDemo.class.getResource("/GPUdataset_2015.csv");
        File file = new File(url.getFile());

        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.buildFromCSV(file);
        System.out.println(multitemporalTradespace.toString());

    }

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

}
