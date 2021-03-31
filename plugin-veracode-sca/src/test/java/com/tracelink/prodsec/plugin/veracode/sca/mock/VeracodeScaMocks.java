package com.tracelink.prodsec.plugin.veracode.sca.mock;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueType;
import java.time.LocalDateTime;
import java.util.UUID;

public class VeracodeScaMocks {

	private static final String MAIN = "main";

	public static VeracodeScaIssue mockVulnerabilityIssue() {
		VeracodeScaIssue issue = mockBaseIssue();
		issue.setIssueType(IssueType.VULNERABILITY);
		return issue;
	}

	public static VeracodeScaIssue mockLicenseIssue() {
		VeracodeScaIssue issue = mockBaseIssue();
		issue.setIssueType(IssueType.LICENSE);
		return issue;
	}

	private static VeracodeScaIssue mockBaseIssue() {
		VeracodeScaIssue issue = new VeracodeScaIssue();
		issue.setId(UUID.randomUUID());
		issue.setLastUpdatedDate(LocalDateTime.now());
		issue.setProjectBranch(MAIN);
		issue.setIssueStatus(IssueStatus.OPEN);
		return issue;
	}

	public static VeracodeScaProject mockProject() {
		VeracodeScaProject project = new VeracodeScaProject();
		project.setId(UUID.randomUUID());
		project.setName("Mock Project");
		project.setVisibleBranch(MAIN);
		return project;
	}

}
