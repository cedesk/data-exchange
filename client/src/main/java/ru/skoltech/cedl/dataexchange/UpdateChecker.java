package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by D.Knoll on 28.11.2015.
 */
public class UpdateChecker {

    private static final Logger logger = Logger.getLogger(UpdateChecker.class);

    public static Optional<ApplicationPackage> getLatestVersionAvailable() {
        String serverString = ApplicationProperties.getAppDistributionServerUrl();
        try {
            URL serverUrl = new URL(serverString);
            Document doc = getDocument(serverUrl);
            List<String> fileNames = extractFileNames(doc);

            ApplicationPackage applicationPackage = getLatest(fileNames);
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

    public static ApplicationPackage getLatest(List<String> fileNames) {
        ApplicationPackage latestAppPkg = null;
        for (String filename : fileNames) {
            ApplicationPackage applicationPackage = ApplicationPackage.fromFileName("", filename);
            if ((latestAppPkg == null) || (latestAppPkg.compareTo(applicationPackage) < 0)) {
                latestAppPkg = applicationPackage;
            }
        }
        return latestAppPkg;
    }

    public static List<String> extractFileNames(File file) throws IOException {
        Document doc = Jsoup.parse(file, Charset.defaultCharset().name());
        return extractFileNames(doc);
    }

    private static List<String> extractFileNames(Document doc) {
        String fileExtension = ApplicationPackage.getExtension();
        List<String> fileList = new LinkedList<>();
        Elements links = doc.select("a[href$=." + fileExtension + "]");
        for (Element link : links) {
            fileList.add(link.text());
        }
        return fileList;
    }

    private static Document getDocument(URL url) throws IOException {
        return Jsoup.parse(url, 3000);
    }
}
