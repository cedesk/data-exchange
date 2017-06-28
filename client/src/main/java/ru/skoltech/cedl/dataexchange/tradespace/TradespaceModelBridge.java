package ru.skoltech.cedl.dataexchange.tradespace;

import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterTreeIterator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by d.knoll on 6/28/2017.
 */
public class TradespaceModelBridge {

    public static Collection<ParameterModel> getModelOutputParameters() {
        Project project = ProjectContext.getInstance().getProject();
        ModelNode systemModel = project.getSystemModel();
        List<ParameterModel> parameters = new LinkedList<>();
        ParameterTreeIterator subsystemParameterIterator = new ParameterTreeIterator(systemModel,
                parameterModel -> parameterModel.getNature() == ParameterNature.OUTPUT);
        subsystemParameterIterator.forEachRemaining(parameters::add);
        return parameters;
    }

    public static String getParameterName(String parameterUuid) {
        if (parameterUuid == null) return "<not defined>";
        Project project = ProjectContext.getInstance().getProject();
        Map<String, ParameterModel> parameterDictionary = project.getStudy().getSystemModel().makeParameterDictionary();

        ParameterModel parameterModel = parameterDictionary.get(parameterUuid);
        if (parameterModel != null) {
            return parameterModel.getNodePath();
        }
        return "<not found>";
    }
}
