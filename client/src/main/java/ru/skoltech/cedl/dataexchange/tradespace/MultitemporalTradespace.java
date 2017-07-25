package ru.skoltech.cedl.dataexchange.tradespace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class MultitemporalTradespace {

    private List<Epoch> epochs = new LinkedList<>();
    private List<FigureOfMeritDefinition> definitions = new LinkedList<>();
    private List<DesignPoint> designPoints = new LinkedList<>();

    public MultitemporalTradespace() {
    }

    public List<FigureOfMeritDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<FigureOfMeritDefinition> definitions) {
        this.definitions = definitions;
    }

    public Map<String, FigureOfMeritDefinition> getDefinitionsMap() {
        return definitions.stream().collect(Collectors.toMap(FigureOfMeritDefinition::getName, Function.identity()));
    }

    public List<DesignPoint> getDesignPoints() {
        return designPoints;
    }

    public void setDesignPoints(List<DesignPoint> designPoints) {
        this.designPoints = designPoints;
    }

    public List<Epoch> getEpochs() {
        return epochs;
    }

    public void setEpochs(List<Epoch> epochs) {
        this.epochs = epochs;
    }

    public Epoch getEpoch(int index) {
        return epochs.get(index);
    }

    @Override
    public String toString() {
        return "MultitemporalTradespace{" +
                "epochs=" + epochs +
                ", definitions=" + definitions +
                ", designPoints=\n" + designPoints +
                '}';
    }
}
