package com.tracelink.prodsec.plugin.veracode.sca.util.model;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

/**
 * IssueSummaries
 */
public class IssueSummaries {

	@SerializedName("issues")
	private List<IssueSummary> issues = Collections.emptyList();

	public List<IssueSummary> getIssues() {
		return issues;
	}

	public void setIssues(List<IssueSummary> issues) {
		this.issues = issues;
	}
}
