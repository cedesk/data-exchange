/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services;

import javax.persistence.EntityManager;

/**
 * Operations in JPA repository.
 *
 * Created by Nikolay Groshkov on 10-Jul-17.
 */
public interface PersistenceRepositoryService extends RepositoryService {

    /**
     * Setup acutual {@link EntityManager} to perform operations.
     *
     * @param entityManager actual {@link EntityManager}
     */
    void setEntityManager(EntityManager entityManager);
}
