package com.tracelink.prodsec.plugin.bsimm.service;

/**
 * Thrown if a survey does not yet have all responses and is saved
 * 
 * @author csmith
 *
 */
public class SurveyIncompleteException extends Exception {

	private static final long serialVersionUID = 2625110012660094262L;

	public SurveyIncompleteException(String string) {
		super(string);
	}

}
