package com.tracelink.prodsec.plugin.sme.service;

/**
 * Thrown when a SME's state is unexpected. E.g. should exist, but doesn't or
 * when it shouldn't exist but does
 * 
 * @author csmith
 *
 */
public class SMEException extends Exception {

	private static final long serialVersionUID = -378426701689495745L;

	public SMEException(String message) {
		super(message);
	}
}
