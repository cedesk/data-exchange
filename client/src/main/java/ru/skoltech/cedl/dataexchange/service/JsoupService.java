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

package ru.skoltech.cedl.dataexchange.service;

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
