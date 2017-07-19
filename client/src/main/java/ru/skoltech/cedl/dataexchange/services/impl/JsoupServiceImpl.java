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
 * Created by n.groshkov on 19-Jul-17.
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
