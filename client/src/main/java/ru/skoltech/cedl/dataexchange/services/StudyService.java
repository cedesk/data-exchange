package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

/**
 * Operations with {@link Study}.
 *
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface StudyService {

    /**
     * Create {@link Study} based on {@link SystemModel} and {@link UserManagement}.
     *
     * @param systemModel
     * @param userManagement
     * @return new instance of {@link Study}
     */
    Study createStudy(SystemModel systemModel, UserManagement userManagement);
}
