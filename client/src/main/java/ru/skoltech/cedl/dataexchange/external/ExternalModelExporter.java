package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.Closeable;

/**
 * Created by D.Knoll on 23.07.2015.
 */
public interface ExternalModelExporter extends Closeable {

    void setValue(String target, Double value) throws ExternalModelException;

    void flushModifications(Project project, ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException;
}
