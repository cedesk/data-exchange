package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public interface Repository {

    SystemModel loadSystemModel();

    void storeSystemModel(SystemModel systemModel);

    void close();

    Study loadStudy(String name);

    void storeStudy(Study study);
}
