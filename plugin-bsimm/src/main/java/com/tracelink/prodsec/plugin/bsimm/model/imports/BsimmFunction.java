package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.List;

public class BsimmFunction {
	private String functionName;

	private List<BsimmPractice> practices;

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public List<BsimmPractice> getPractices() {
		return practices;
	}

	public void setPractices(List<BsimmPractice> practices) {
		this.practices = practices;
	}

}
