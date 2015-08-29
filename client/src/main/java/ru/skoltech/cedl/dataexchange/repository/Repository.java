package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.io.Closeable;
import java.util.List;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public interface Repository extends Closeable {

    SystemModel loadSystemModel(long studyId) throws RepositoryException;

    UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException;

    SystemModel storeSystemModel(SystemModel systemModel) throws RepositoryException;

    List<String> listStudies() throws RepositoryException;

    Study loadStudy(String name) throws RepositoryException;

    Study storeStudy(Study study) throws RepositoryException;

    UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException;

    UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException;

    UserManagement loadUserManagement() throws RepositoryException;

    UnitManagement storeUnitManagement(UnitManagement unitManagement) throws RepositoryException;

    UnitManagement loadUnitManagement() throws RepositoryException;

    List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException;

    String getUrl();

    ExternalModel storeExternalModel(ExternalModel externalModel) throws RepositoryException;

    ExternalModel loadExternalModel(long externalModelId) throws RepositoryException;
}
