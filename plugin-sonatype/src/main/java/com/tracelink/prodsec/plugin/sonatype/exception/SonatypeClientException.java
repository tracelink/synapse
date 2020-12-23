package com.tracelink.prodsec.plugin.sonatype.exception;

/**
 * Exception thrown when there is no Sonatype API client configured in the
 * database.
 *
 * @author mcool
 */
public class SonatypeClientException extends RuntimeException {
    private static final long serialVersionUID = 2180097275755026110L;

    /**
     * Creates an exception with the given message.
     *
     * @param message string indicating reason for exception thrown
     */
    public SonatypeClientException(String message) {
        super(message);
    }
}
