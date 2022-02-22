package com.tracelink.prodsec.plugin.veracode.dast.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;

/**
 * The Report contains the high-level view of all flaws found in a scan for a
 * given Veracode App. Includes Date of report, violation counts, and policy
 * score
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = VeracodeDastPlugin.SCHEMA, name = "veracode_dast_reports")
public class VeracodeDastReportModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id")
	private long id;

	@Column(name = "report_date")
	private LocalDateTime reportDate;

	@Column(name = "build_id")
	private long buildId;

	@Column(name = "analysis_id")
	private long analysisId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "app", nullable = false)
	private VeracodeDastAppModel app;

	@Column(name = "policy_score")
	private long policyScore;

	@Column(name = "unmitigated")
	private long unmitigated;

	@Column(name = "total_flaws")
	private long totalFlaws;
	
	@Column(name = "report_coordinates")
	private String coordinates;

	public long getId() {
		return id;
	}

	public LocalDateTime getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDateTime reportDate) {
		this.reportDate = reportDate;
	}

	public long getBuildId() {
		return buildId;
	}

	public void setBuildId(long buildId) {
		this.buildId = buildId;
	}

	public long getAnalysisId() {
		return analysisId;
	}

	public void setAnalysisId(long analysisId) {
		this.analysisId = analysisId;
	}

	public VeracodeDastAppModel getApp() {
		return app;
	}

	public void setApp(VeracodeDastAppModel app) {
		this.app = app;
	}

	public void setScore(long score) {
		policyScore = score;
	}

	public long getScore() {
		return policyScore;
	}

	public void setTotalFlaws(Long totalFlaws) {
		this.totalFlaws = totalFlaws;
	}

	public long getTotalFlaws() {
		return this.totalFlaws;
	}
	
	public void setUnmitigatedFlaws(Long flawsNotMitigated) {
		this.unmitigated = flawsNotMitigated;
	}

	public long getUnmitigatedFlaws() {
		return this.unmitigated;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

}
