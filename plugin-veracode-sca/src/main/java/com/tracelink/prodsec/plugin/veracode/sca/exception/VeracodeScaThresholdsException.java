package com.tracelink.prodsec.plugin.veracode.sca.exception;

/**
 * Exception thrown when there are no risk thresholds configured in the database.
 *
 * @author mcool
 */
public class VeracodeScaThresholdsException extends RuntimeException {

	private static final long serialVersionUID = -6323515428491919215L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public VeracodeScaThresholdsException(String message) {
		super(message);
	}
}
