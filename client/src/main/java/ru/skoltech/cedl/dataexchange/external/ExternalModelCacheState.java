/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public enum ExternalModelCacheState {
    NOT_CACHED,
    CACHED_CONFLICTING_CHANGES,
    CACHED_MODIFIED_AFTER_CHECKOUT,
    CACHED_OUTDATED,
    CACHED_UP_TO_DATE
}
