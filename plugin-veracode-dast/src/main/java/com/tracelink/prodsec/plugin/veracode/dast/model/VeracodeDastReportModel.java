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

	@Column(name = "very_high_vios")
	private long veryHighVios;

	@Column(name = "high_vios")
	private long highVios;

	@Column(name = "med_vios")
	private long medVios;

	@Column(name = "low_vios")
	private long lowVios;

	@Column(name = "very_low_vios")
	private long veryLowVios;

	@Column(name = "info_vios")
	private long infoVios;

	@Column(name = "policy_score")
	private long policyScore;
	
	@Column(name="reportUrl")
	private String reportUrl;

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

	public long getVeryHighVios() {
		return veryHighVios;
	}

	public void setVeryHighVios(long veryHighVios) {
		this.veryHighVios = veryHighVios;
	}

	public long getHighVios() {
		return highVios;
	}

	public void setHighVios(long highVios) {
		this.highVios = highVios;
	}

	public long getMedVios() {
		return medVios;
	}

	public void setMedVios(long medVios) {
		this.medVios = medVios;
	}

	public long getLowVios() {
		return lowVios;
	}

	public void setLowVios(long lowVios) {
		this.lowVios = lowVios;
	}

	public long getVeryLowVios() {
		return veryLowVios;
	}

	public void setVeryLowVios(long veryLowVios) {
		this.veryLowVios = veryLowVios;
	}

	public long getInfoVios() {
		return infoVios;
	}

	public void setInfoVios(long infoVios) {
		this.infoVios = infoVios;

	}

	public void setScore(long score) {
		policyScore = score;
	}

	public long getScore() {
		return policyScore;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	public long getVulnerabilitiesCount() {
		return veryHighVios + highVios + medVios + lowVios + veryLowVios + infoVios;
	}

}
