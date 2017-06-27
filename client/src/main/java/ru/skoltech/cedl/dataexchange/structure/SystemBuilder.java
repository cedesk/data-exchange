package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by d.knoll on 27/06/2017.
 */
public abstract class SystemBuilder {

    public static final int MIN_MODEL_DEPTH = 1;
    public static final int MAX_MODEL_DEPTH = 4;
    static int systemsCnt = 1;
    static int parameterCnt = 1;
    static int elementCnt = 1;
    static int instrumentCnt = 1;

    static ParameterModel getMassParameter() {
        ParameterModel parameterModel = new ParameterModel("mass", getRandomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        return parameterModel;
    }

    public abstract String getName();

    static ParameterModel getPowerParameter() {
        ParameterModel parameterModel = new ParameterModel("power", getRandomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        return parameterModel;
    }

    static double getRandomDouble() {
        return Math.round(Math.random() * 1000) / 10;
    }

    public abstract SystemModel build();
}
