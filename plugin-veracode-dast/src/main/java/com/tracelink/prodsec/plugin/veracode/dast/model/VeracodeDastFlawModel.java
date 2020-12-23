package com.tracelink.prodsec.plugin.veracode.dast.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;

/**
 * A Flaw stores problems found in a report and a link back to its originating
 * report
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = VeracodeDastPlugin.SCHEMA, name = "veracode_dast_flaws")
public class VeracodeDastFlawModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "flaw_id")
	private long id;

	@Column(name = "analysis_id")
	private long analysisId;

	@Column(name = "issue_id")
	private long issueId;

	@Column(name = "category_name")
	private String categoryName;

	@Column(name = "cwe_id")
	private long cweId;

	@Column(name = "cwe_name")
	private String cweName;

	@Column(name = "severity")
	private int severity;

	@Column(name = "count")
	private int count;

	@Column(name = "remediation_status")
	private String remediationStatus;

	@Column(name = "mitigation_status")
	private String mitigationStatus;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "report", nullable = false)
	private VeracodeDastReportModel report;

	public long getId() {
		return id;
	}

	public long getAnalysisId() {
		return this.analysisId;
	}

	public void setAnalysisId(long analysisId) {
		this.analysisId = analysisId;
	}

	public long getIssueId() {
		return this.issueId;
	}

	public void setIssueId(long issueId) {
		this.issueId = issueId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public long getCweId() {
		return cweId;
	}

	public void setCweId(long cweId) {
		this.cweId = cweId;
	}

	public String getCweName() {
		return cweName;
	}

	public void setCweName(String cweName) {
		this.cweName = cweName;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getSeverityString() {
		switch (severity) {
		case 0:
			return "Info";
		case 1:
			return "Very Low";
		case 2:
			return "Low";
		case 3:
			return "Medium";
		case 4:
			return "High";
		case 5:
			return "Very High";
		}
		return "";
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getRemediationStatus() {
		return remediationStatus;
	}

	public void setRemediationStatus(String remediationStatus) {
		this.remediationStatus = remediationStatus;
	}

	public String getMitigationStatus() {
		return mitigationStatus;
	}

	public void setMitigationStatus(String mitigationStatus) {
		this.mitigationStatus = mitigationStatus;
	}

	public VeracodeDastReportModel getReport() {
		return report;
	}

	public void setReport(VeracodeDastReportModel report) {
		this.report = report;
	}

	/**
	 * These are the options for the remediation status: Cannot Reproduce, Fixed,
	 * Mitigated, Potential False Positive, Remediated by User, New, Open, Reopened
	 * (Re-Open for V4 of the API and earlier), Reviewed - No Action Taken
	 * 
	 * The first 5 count as a remediation of the issue.
	 * 
	 * @return true if the flaw has been remediated, false otherwise
	 */
	public boolean isRemediated() {
		String lower = this.remediationStatus.toLowerCase();
		return lower.startsWith("cannot") || lower.startsWith("fixed") || lower.startsWith("mitigated")
				|| lower.startsWith("potential") || lower.startsWith("remediated");

	}

}
