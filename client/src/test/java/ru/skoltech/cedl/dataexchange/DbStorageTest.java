package ru.skoltech.cedl.dataexchange;

import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DBUtil;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by dknoll on 23/05/15.
 */
public class DbStorageTest {

    @Test
    public void storeAndRetrieve() {
        //Study study = DBUtil.loadStudy();

        //System.out.println(study);
        SystemModel systemModel = DummySystemBuilder.getSystemModel(4);
        System.out.println(systemModel);
        DBUtil.storeModel(systemModel);
    }
}
