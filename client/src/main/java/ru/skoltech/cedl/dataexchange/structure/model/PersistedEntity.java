/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * The most essential common feature of all persisted entities is that they have an ID.
 * <p>
 * Created by d.knoll on 18/05/2017.
 */
public interface PersistedEntity {

    long getId();
}
