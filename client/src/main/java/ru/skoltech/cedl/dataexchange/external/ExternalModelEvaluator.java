package ru.skoltech.cedl.dataexchange.external;

import java.io.Closeable;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public interface ExternalModelEvaluator extends Closeable {

    Double getValue(String target) throws ExternalModelException;
}
