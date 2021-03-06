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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.skoltech.cedl.dataexchange.ApplicationPackage;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.JsoupService;
import ru.skoltech.cedl.dataexchange.service.UpdateService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link UpdateService}
 * <p>
 * Created by D.Knoll on 28.11.2015.
 */
public class UpdateServiceImpl implements UpdateService {

    private static final Logger logger = Logger.getLogger(UpdateServiceImpl.class);

    private ApplicationSettings applicationSettings;
    private JsoupService jsoupService;

    @Override
    public Optional<ApplicationPackage> getLatestVersionAvailable() {
        String serverString = applicationSettings.getApplicationDistributionServerUrl();
        try {
            URL serverUrl = new URL(serverString);
            Document doc = jsoupService.jsoupParse(serverUrl);
            List<Pair<String, String>> pairs = this.extractFileNames(doc);

            ApplicationPackage applicationPackage = this.getLatest(pairs);
            return Optional.of(applicationPackage);
        } catch (MalformedURLException e) {
            logger.error("error with application distribution server url: " + e.getMessage());
        } catch (IOException e) {
            logger.error("problem accessing application distribution server: " + e.getMessage());
        } catch (Exception e) {
            logger.error("unknown problem while checking available software on distribution server: " + serverString, e);
        }
        return Optional.empty();
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setJsoupService(JsoupService jsoupService) {
        this.jsoupService = jsoupService;
    }

    @Override
    public List<Pair<String, String>> extractFileNamesAndLinks(File file) throws IOException {
        Document doc = jsoupService.jsoupParse(file);
        return extractFileNames(doc);
    }

    @Override
    public ApplicationPackage getLatest(List<Pair<String, String>> pairs) {
        return pairs.stream()
                .map(pair -> ApplicationPackage.fromFileName(pair.getRight(), pair.getLeft()))
                .filter(Objects::nonNull)
                .max(ApplicationPackage::compareTo)
                .orElse(null);
    }

    private List<Pair<String, String>> extractFileNames(Document doc) {
        String fileExtension = ApplicationPackage.getExtension();
        List<Pair<String, String>> fileList = new LinkedList<>();
        if (doc == null) {
            return fileList;
        }
        Elements links = doc.select("a");
        for (Element link : links) {
            if (link.text().endsWith(fileExtension)) {
                fileList.add(new ImmutablePair<>(link.text(), link.attr("href")));
            }
        }
        return fileList;
    }

}
