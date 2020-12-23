package com.tracelink.prodsec.plugin.veracode.sca.exception;

/**
 * Exception thrown when there is no Veracode SCA API client configured in the database.
 *
 * @author mcool
 */
public class VeracodeScaClientException extends RuntimeException {

	private static final long serialVersionUID = 1188598188403440741L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public VeracodeScaClientException(String message) {
		super(message);
	}
}
