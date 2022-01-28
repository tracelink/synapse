package com.tracelink.prodsec.lib.veracode.rest.api;

/**
 * Exception thrown when there is an error connecting to the Veracode SCA server via the API client.
 *
 * @author mcool
 */
public class VeracodeRestApiException extends RuntimeException {

	private static final long serialVersionUID = -7287664333858461439L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	VeracodeRestApiException(String message) {
		super(message);
	}
}
