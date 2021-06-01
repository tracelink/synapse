package com.tracelink.prodsec.plugin.veracode.sast.model;

/**
 * Exception thrown when there is a problem creating or updating a Veracode SCA workspace, project
 * or issue.
 *
 * @author mcool
 */
public class VeracodeSastProductException extends RuntimeException {

	private static final long serialVersionUID = 7795521113626260081L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public VeracodeSastProductException(String message) {
		super(message);
	}
}
