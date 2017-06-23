package ru.skoltech.cedl.dataexchange.tradespace;

import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class MultitemporalTradespace {

    private List<Epoch> epochs;

    private List<FigureOfMeritDefinition> definitions;

    private List<DesignPoint> designPoints;

    public List<Epoch> getEpochs() {
        return epochs;
    }

    public void setEpochs(List<Epoch> epochs) {
        this.epochs = epochs;
    }

    public Epoch getEpoch(int index) {
        return epochs.get(index);
    }

    public List<FigureOfMeritDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<FigureOfMeritDefinition> definitions) {
        this.definitions = definitions;
    }

    public List<DesignPoint> getDesignPoints() {
        return designPoints;
    }

    public void setDesignPoints(List<DesignPoint> designPoints) {
        this.designPoints = designPoints;
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
