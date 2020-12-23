package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.List;

/**
 * The BSIMM survey is a set of functions, practices, levels and measures that
 * are responded to in order to generate a maturity score.
 * 
 * This object also holds a list of firms to compare against
 * 
 * @author csmith
 *
 */
public class BsimmSurvey {
	private String surveyName;

	private List<BsimmFunction> functions;

	private List<BsimmComparison> comparisons;

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public List<BsimmFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<BsimmFunction> functions) {
		this.functions = functions;
	}

	public List<BsimmComparison> getComparisons() {
		return comparisons;
	}

	public void setComparisons(List<BsimmComparison> comparisons) {
		this.comparisons = comparisons;
	}

	public void validate() throws SurveyImportException {
		for (BsimmComparison compare : comparisons) {
			compare.validate(this);
		}
	}
}
