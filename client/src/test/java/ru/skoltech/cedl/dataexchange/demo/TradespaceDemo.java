package ru.skoltech.cedl.dataexchange.demo;

import ru.skoltech.cedl.dataexchange.tradespace.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceDemo {

    public static void main(String... args) {

    }

    private static MultitemporalTradespace buildTradespace() {
        MultitemporalTradespace multitemporalTradespace = new MultitemporalTradespace();
        List<Epoch> epoches = buildEpochs(2017, 2020, 2025, 2030);
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

    private static List<Epoch> buildEpochs(Integer... years) {
        List<Epoch> epochs = new ArrayList<>(years.length);
        for (Integer year : years) {
            epochs.add(new Epoch(year));
        }
        return epochs;
    }
}
