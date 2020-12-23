package com.tracelink.prodsec.plugin.veracode.sca.model.issue;

/**
 * Represents the type of issue reported by Veracode: either a library issue, a license issue, or a
 * vulnerability issue.
 *
 * @author mcool
 */
public enum IssueType {
	LIBRARY("Library"),
	LICENSE("License"),
	VULNERABILITY("Vulnerability");

	private final String value;

	IssueType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
