package com.tracelink.prodsec.plugin.bsimm.service;

/**
 * Thrown for all exceptions related to survey processing or storing
 * 
 * @author csmith
 *
 */
public class SurveyException extends Exception {

	private static final long serialVersionUID = 2753421901109844943L;

	public SurveyException(String string) {
		super(string);
	}

}
