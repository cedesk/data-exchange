/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Access to <b>jsoup</b> parser library.
 *
 * Created by Nikolay Groshkov on 19-Jul-17.
 */
public interface JsoupService {

    /**
     * Parse remote page by <b>jsoup</b> parser.
     *
     * @param url of page
     * @return parsed page
     * @throws IOException if page in unavailable of cannot be parsed.
     */
    Document jsoupParse(URL url) throws IOException;

    /**
     * Parse local file by <b>jsoup</b> parser.
     *
     * @param in file
     * @return parsed file
     * @throws IOException if file in unavailable of cannot be parsed.
     */
    Document jsoupParse(File in) throws IOException;
}
