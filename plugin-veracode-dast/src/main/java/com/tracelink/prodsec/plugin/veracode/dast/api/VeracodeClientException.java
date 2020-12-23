package com.tracelink.prodsec.plugin.veracode.dast.api;

/**
 * Thrown if there are any issues encountered with any API call to Veracode
 * 
 * @author csmith
 *
 */
public class VeracodeClientException extends Exception {

	private static final long serialVersionUID = 3594695833129688506L;

	public VeracodeClientException(String message, Exception e) {
		super(message, e);
	}

	public VeracodeClientException(String message) {
		super(message);
	}

}
