package com.tracelink.prodsec.plugin.veracode.sca.model.issue;

/**
 * Represents the status of an issue, indicating whether it is still an open issue or if it has
 * been fixed. Note that an issue can have a status of open, but be ignored. In this case the
 * issue is "resolved", but not fixed.
 *
 * @author mcool
 */
public enum IssueStatus {
	FIXED, OPEN
}
