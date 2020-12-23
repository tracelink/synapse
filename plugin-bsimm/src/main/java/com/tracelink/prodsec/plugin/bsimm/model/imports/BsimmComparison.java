package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.ArrayList;
import java.util.List;

/**
 * A Comparison is a set of measures and scores for different "firms" in the
 * BSIMM report
 *
 * @author csmith
 */
public class BsimmComparison {

	private String comparisonTitle;

	private List<BsimmComparisonFunction> functions;

	public String getComparisonTitle() {
		return comparisonTitle;
	}

	public void setComparisonTitle(String comparisonTitle) {
		this.comparisonTitle = comparisonTitle;
	}

	public List<BsimmComparisonFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<BsimmComparisonFunction> functions) {
		this.functions = functions;
	}

	public void validate(BsimmSurvey bsimmSurvey) throws SurveyImportException {
		List<BsimmFunction> surveyFunctions = new ArrayList<>(bsimmSurvey.getFunctions());
		int surveySize = surveyFunctions.size();

		if (surveySize != functions.size()) {
			throw new SurveyImportException(
					"Survey Functions Comparisons don't match number of Functions for the Function: "
							+ this.comparisonTitle);
		}

		for (int i = 0; i < surveySize; i++) {
			BsimmFunction func = surveyFunctions.get(i);
			BsimmComparisonFunction compareFunc = functions.get(i);
			if (func.getFunctionName().equals(compareFunc.getFunctionName())) {
				compareFunc.validate(func);
			} else {
				throw new SurveyImportException("No matching Comparison Function for Function: "
						+ func.getFunctionName()
						+ ". These must be in the same order as the survey");
			}
		}
	}
}
