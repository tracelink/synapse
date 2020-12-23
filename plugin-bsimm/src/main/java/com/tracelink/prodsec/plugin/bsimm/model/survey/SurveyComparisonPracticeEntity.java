package com.tracelink.prodsec.plugin.bsimm.model.survey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;

/**
 * Holds a comparison score for a response to a BSIMM survey version
 * 
 * This contains response scores for each comparison for each function's
 * practice.
 * 
 * Has many to one with the {@linkplain SurveyEntity} that this relates to
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = BSIMMPlugin.SCHEMA, name = "survey_comparison_practice")
public class SurveyComparisonPracticeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "survey_compare_practice_id")
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "comparison_id")
	private SurveyComparisonEntity comparison;

	@Column(name = "function_name")
	private String functionName;

	@Column(name = "practice_name")
	private String practiceName;

	@Column(name = "practice_score")
	private double score;

	public long getId() {
		return id;
	}

	public SurveyComparisonEntity getComparison() {
		return comparison;
	}

	public void setComparison(SurveyComparisonEntity comparison) {
		this.comparison = comparison;
	}

	public String getFunctionName() {
		return this.functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getPracticeName() {
		return practiceName;
	}

	public void setPracticeName(String practiceName) {
		this.practiceName = practiceName;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

}
