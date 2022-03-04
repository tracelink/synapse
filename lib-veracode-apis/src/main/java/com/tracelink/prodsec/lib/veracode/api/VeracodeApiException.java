package com.tracelink.prodsec.lib.veracode.api;

/**
 * Thrown if any api encounters an error
 * 
 * @author csmith
 *
 */
public class VeracodeApiException extends Exception {

	private static final long serialVersionUID = -3399089830556894822L;

	public VeracodeApiException(String message, Exception e) {
		super(message, e);
	}

	public VeracodeApiException(String message) {
		super(message);
	}

}
