package ru.skoltech.cedl.dataexchange.external;

import java.io.Closeable;

/**
 * Created by D.Knoll on 23.07.2015.
 */
public interface ExternalModelExporter extends Closeable {

    void setValue(String target, Double value) throws ExternalModelException;

    void flushModifications() throws ExternalModelException;
}
