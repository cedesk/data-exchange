package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.Closeable;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public interface ExternalModelEvaluator extends Closeable {

    Double getValue(Project project, String target) throws ExternalModelException;
}
