package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hripsime.matevosyan on 3/26/2015.
 */
public class DiffModel extends ModelNode {

    public DiffModel(ModelNode n1, ModelNode n2) {
        List<ParameterModel> l1 = n1.getParameters();
        List<ParameterModel> l2 = n2.getParameters();


        Set<ParameterModel> diff = Utils.symmetricDiffTwoLists(l1, l2);

        Map<String, ParameterModel> map1 = l1.stream().collect(
                Collectors.toMap(ParameterModel::getName, (m) -> m)
        );

        Map<String, ParameterModel> map2 = l2.stream().collect(
                Collectors.toMap(ParameterModel::getName, (m) -> m)
        );

        // TODO: Utilize the created containers to initialize the model parameters
    }
}
