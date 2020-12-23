package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.List;

public class BsimmComparisonFunction {
	private String functionName;
	private List<BsimmComparisonPractice> practices;

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public List<BsimmComparisonPractice> getPractices() {
		return practices;
	}

	public void setPractices(List<BsimmComparisonPractice> practices) {
		this.practices = practices;
	}

	public void validate(BsimmFunction func) throws SurveyImportException {
		List<BsimmPractice> surveyPractices = func.getPractices();
		int practiceSize = surveyPractices.size();

		if (practiceSize != practices.size()) {
			throw new SurveyImportException(
					"Survey Practice Comparisons don't match number of Practices for the Function: "
							+ this.functionName);
		}

		for (int i = 0; i < practiceSize; i++) {
			BsimmPractice prac = surveyPractices.get(i);
			BsimmComparisonPractice comparePrac = practices.get(i);
			if (!prac.getPracticeName().equals(comparePrac.getPracticeName())) {
				throw new SurveyImportException("No matching Comparison Practice for Practice: "
						+ prac.getPracticeName() + ". These must be in the same order as the survey");
			}
		}
	}
}
