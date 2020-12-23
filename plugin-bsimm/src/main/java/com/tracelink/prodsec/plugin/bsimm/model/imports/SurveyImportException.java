package com.tracelink.prodsec.plugin.bsimm.model.imports;

/**
 * Thrown if any problem occurs during import of the survey xml
 * 
 * @author csmith
 *
 */
public class SurveyImportException extends Exception {

	private static final long serialVersionUID = 8147240374889729947L;

	public SurveyImportException(String string) {
		super(string);
	}
}
