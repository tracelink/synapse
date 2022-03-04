package com.tracelink.prodsec.lib.veracode.api.rest.model;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

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
