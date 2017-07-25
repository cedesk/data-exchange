/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.skoltech.cedl.dataexchange.services.JsoupService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Implementation of {@link JsoupService}.
 *
 * Created by Nikolay Groshkov on 19-Jul-17.
 */
public class JsoupServiceImpl implements JsoupService {

    private static final int TIMEOUT_MILLIS = 10000;

    @Override
    public Document jsoupParse(URL url) throws IOException{
        return Jsoup.parse(url, TIMEOUT_MILLIS);
    }

    @Override
    public Document jsoupParse(File in) throws IOException {
        return Jsoup.parse(in, Charset.defaultCharset().name());

    }
}
