package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.skoltech.cedl.dataexchange.ApplicationPackage;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.services.JsoupService;
import ru.skoltech.cedl.dataexchange.services.UpdateService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
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
            Document doc = getDocument(serverUrl);
            List<String> fileNames = this.extractFileNames(doc);

            ApplicationPackage applicationPackage = this.getLatest(fileNames);
            applicationPackage.setBaseUrl(serverString);
            return Optional.of(applicationPackage);
        } catch (MalformedURLException e) {
            logger.error("error with application distribution server url: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("problem accessing application distribution server: " + e.getMessage(), e);
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

    private Document getDocument(URL url) throws IOException {
        return jsoupService.jsoupParse(url);
    }
}
