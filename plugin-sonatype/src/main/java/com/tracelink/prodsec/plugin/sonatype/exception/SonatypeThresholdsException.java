package com.tracelink.prodsec.plugin.sonatype.exception;

/**
 * Exception thrown when there are no violation tolerance thresholds configured
 * in the database.
 *
 * @author mcool
 */
public class SonatypeThresholdsException extends RuntimeException {
    private static final long serialVersionUID = -6323515428491919215L;

    /**
     * Creates an exception with the given message.
     *
     * @param message string indicating reason for exception thrown
     */
    public SonatypeThresholdsException(String message) {
        super(message);
    }
}
