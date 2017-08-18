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
import java.util.Optional;

/**
 * Implementation of {@link UpdateService}
 *
 * Created by D.Knoll on 28.11.2015.
 */
public class UpdateServiceImpl implements UpdateService {

    private static final Logger logger = Logger.getLogger(UpdateServiceImpl.class);

    private ApplicationSettings applicationSettings;
    private JsoupService jsoupService;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setJsoupService(JsoupService jsoupService) {
        this.jsoupService = jsoupService;
    }

    @Override
    public Optional<ApplicationPackage> getLatestVersionAvailable() {
        String serverString = applicationSettings.getApplicationDistributionServerUrl();
        try {
            URL serverUrl = new URL(serverString);
            Document doc = jsoupService.jsoupParse(serverUrl);
            List<String> fileNames = this.extractFileNames(doc);

            ApplicationPackage applicationPackage = this.getLatest(fileNames);
            applicationPackage.setBaseUrl(serverString);
            return Optional.of(applicationPackage);
        } catch (MalformedURLException e) {
            logger.error("error with application distribution server url: " + e.getMessage());
        } catch (IOException e) {
            logger.error("problem accessing application distribution server: " + e.getMessage());
        } catch (Exception e) {
            logger.error("unknown problem while checking available software on distribution server", e);
        }
        return Optional.empty();
    }

    @Override
    public ApplicationPackage getLatest(List<String> fileNames) {
        ApplicationPackage latestAppPkg = null;
        for (String filename : fileNames) {
            ApplicationPackage applicationPackage = ApplicationPackage.fromFileName("", filename);
            if ((latestAppPkg == null) || (latestAppPkg.compareTo(applicationPackage) < 0)) {
                latestAppPkg = applicationPackage;
            }
        }
        return latestAppPkg;
    }

    @Override
    public List<String> extractFileNames(File file) throws IOException {
        Document doc = jsoupService.jsoupParse(file);
        return extractFileNames(doc);
    }

    private List<String> extractFileNames(Document doc) {
        String fileExtension = ApplicationPackage.getExtension();
        List<String> fileList = new LinkedList<>();
        Elements links = doc.select("a[href$=." + fileExtension + "]");
        for (Element link : links) {
            fileList.add(link.text());
        }
        return fileList;
    }

}
