package com.tracelink.prodsec.plugin.veracode.sca.model;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueType;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The Database entity for the Veracode SCA issue.
 * <p>
 * Stores a subset of information about the issue.
 *
 * @author mcool
 */
@Entity
@Table(schema = VeracodeScaPlugin.SCHEMA, name = "veracode_sca_issues")
public class VeracodeScaIssue {

	public static final String SEVERITY_HIGH = "High";
	public static final String SEVERITY_MEDIUM = "Medium";
	public static final String SEVERITY_LOW = "Low";

	/**
	 * The ID of this issue, which is a UUID.
	 */
	@Id
	@Column(name = "issue_id")
	private UUID id;

	/**
	 * The date and time this issue was created in the Veracode SCA server.
	 */
	@Column(name = "created_date")
	private LocalDateTime createdDate;

	/**
	 * The date this issue was ignored. It reflects the first time Synapse was notified that the
	 * issue had been ignored. If the issue is not ignored, the date is null. The date is updated
	 * during the periodic data fetch.
	 */
	@Column(name = "ignored_date")
	private LocalDateTime ignoredDate;

	/**
	 * The date this issue was fixed. It reflects the first time Synapse was notified that the
	 * issue had been fixed. If the issue is not fixed, the date is null. The date is updated during
	 * the periodic data fetch.
	 */
	@Column(name = "fixed_date")
	private LocalDateTime fixedDate;

	/**
	 * The last date that this issue was updated during a data fetch. In most cases, this will be
	 * the current date, but if a project or issue is deleted from the Veracode server, the issue
	 * will cease to be updated during the data fetch and this date will remain static.
	 */
	@Column(name = "last_updated_date")
	private LocalDateTime lastUpdatedDate;

	/**
	 * Whether this issue has been ignored on the Veracode SCA server.
	 */
	@Column(name = "ignored")
	private boolean ignored = false;

	/**
	 * The status of this issue: either open or fixed. Note that ignored issues are NOT fixed.
	 */
	@Column(name = "issue_status")
	@Enumerated(value = EnumType.STRING)
	private IssueStatus issueStatus;

	/**
	 * The type of this issue: either library, license, or vulnerability.
	 */
	@Column(name = "issue_type")
	@Enumerated(value = EnumType.STRING)
	private IssueType issueType;

	/**
	 * The branch of the project where this issue was found.
	 */
	@Column(name = "project_branch")
	private String projectBranch;

	/**
	 * The {@link VeracodeScaProject} this issue is associated with.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id")
	private VeracodeScaProject project;

	/**
	 * The severity of this issue, as a decimal number from 0 to 10, where 10 is the most severe.
	 */
	@Column(name = "severity")
	private float severity;

	/**
	 * The type of vulnerability in the library associated with this issue. If this issue is not of
	 * type "vulnerability," then the vulnerability string will be null.
	 */
	@Column(name = "vulnerability")
	private String vulnerability;

	/**
	 * Whether the vulnerability in the library associated with this issue has a vulnerable method.
	 * If this issue is not of type "vulnerability," then the vulnerable method will be null.
	 */
	@Column(name = "vulnerable_method")
	private boolean vulnerableMethod;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getIgnoredDate() {
		return ignoredDate;
	}

	public void setIgnoredDate(LocalDateTime ignoredDate) {
		this.ignoredDate = ignoredDate;
	}

	public LocalDateTime getFixedDate() {
		return fixedDate;
	}

	public void setFixedDate(LocalDateTime fixedDate) {
		this.fixedDate = fixedDate;
	}

	public LocalDateTime getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public IssueStatus getIssueStatus() {
		return issueStatus;
	}

	public void setIssueStatus(IssueStatus issueStatus) {
		this.issueStatus = issueStatus;
	}

	public IssueType getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueType issueType) {
		this.issueType = issueType;
	}

	public String getProjectBranch() {
		return projectBranch;
	}

	public void setProjectBranch(String projectBranch) {
		this.projectBranch = projectBranch;
	}

	public VeracodeScaProject getProject() {
		return project;
	}

	public void setProject(VeracodeScaProject project) {
		this.project = project;
	}

	public float getSeverity() {
		return severity;
	}

	public void setSeverity(float severity) {
		this.severity = severity;
	}

	public String getVulnerability() {
		return vulnerability;
	}

	public void setVulnerability(String vulnerability) {
		this.vulnerability = vulnerability;
	}

	public boolean isVulnerableMethod() {
		return vulnerableMethod;
	}

	public void setVulnerableMethod(boolean vulnerableMethod) {
		this.vulnerableMethod = vulnerableMethod;
	}

	/**
	 * Converts the severity value of this issue into a human-readable string of high, medium, or
	 * low severity.
	 *
	 * @return severity string corresponding to the severity value of this issue
	 */
	public String getSeverityString() {
		if (severity >= 7.0) {
			return SEVERITY_HIGH;
		} else if (severity >= 4.0) {
			return SEVERITY_MEDIUM;
		} else {
			return SEVERITY_LOW;
		}
	}

	/**
	 * Determines whether this issue has been resolved. An issue is resolved if it is either fixed
	 * or ignored.
	 *
	 * @return true if the issue is fixed or ignored, false otherwise
	 */
	public boolean isResolved() {
		return issueStatus.equals(IssueStatus.FIXED) || ignored;
	}
}
