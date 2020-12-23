package com.tracelink.prodsec.plugin.veracode.sca.util.api;

/**
 * Exception thrown when there is an error connecting to the Veracode SCA server via the API client.
 *
 * @author mcool
 */
public class VeracodeScaApiException extends RuntimeException {

	private static final long serialVersionUID = -7287664333858461439L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	VeracodeScaApiException(String message) {
		super(message);
	}
}
