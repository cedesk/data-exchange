/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
