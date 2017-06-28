package ru.skoltech.cedl.dataexchange.structure.model.diff;

/**
 * Created by d.knoll on 6/26/2017.
 */
public class MergeException extends Exception {

    public MergeException(String message) {
        super(message);
    }

    public MergeException(String message, Throwable cause) {
        super(message, cause);
    }
}
