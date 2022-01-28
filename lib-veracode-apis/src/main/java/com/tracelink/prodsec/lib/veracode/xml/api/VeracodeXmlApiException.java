package com.tracelink.prodsec.lib.veracode.xml.api;

/**
 * Thrown if there are any issues encountered with any API call to Veracode
 * 
 * @author csmith
 *
 */
public class VeracodeXmlApiException extends Exception {

	private static final long serialVersionUID = 3594695833129688506L;

	public VeracodeXmlApiException(String message, Exception e) {
		super(message, e);
	}

	public VeracodeXmlApiException(String message) {
		super(message);
	}

}
