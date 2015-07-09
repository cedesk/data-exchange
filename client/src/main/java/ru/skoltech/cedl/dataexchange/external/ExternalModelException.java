package ru.skoltech.cedl.dataexchange.external;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelException extends Exception {
    public ExternalModelException(String message) {
        super(message);
    }

    public ExternalModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
