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
 * Holds the survey Measures
 * 
 * Has many to 1 with the {@linkplain SurveyEntity} itself
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = BSIMMPlugin.SCHEMA, name = "measure")
public class MeasureEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "measure_id")
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owning_survey", nullable = false)
	private SurveyEntity owningSurvey;

	@Column(name = "function")
	private String functionName;

	@Column(name = "practice")
	private String practiceName;

	@Column(name = "level")
	private int level;

	@Column(name = "bsimm_measure_id")
	private String measureId;

	@Column(name = "bsimm_measure_title")
	private String measureTitle;

	@Column(name = "bsimm_measure_detail")
	private String detailMessage;

	public void setOwningSurvey(SurveyEntity owningSurvey) {
		this.owningSurvey = owningSurvey;
	}

	public void setFunction(String functionName) {
		this.functionName = functionName;
	}

	public void setPractice(String practiceName) {
		this.practiceName = practiceName;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setMeasureId(String measureId) {
		this.measureId = measureId;
	}

	public void setMeasureTitle(String measureTitle) {
		this.measureTitle = measureTitle;
	}

	public void setDetail(String detailMessage) {
		this.detailMessage = detailMessage;
	}

	public SurveyEntity getOwningSurvey() {
		return this.owningSurvey;
	}

	public String getFunctionName() {
		return this.functionName;
	}

	public String getPracticeName() {
		return this.practiceName;
	}

	public int getLevel() {
		return this.level;
	}

	public String getMeasureId() {
		return this.measureId;
	}

	public String getMeasureTitle() {
		return this.measureTitle;
	}

	public String getDetailMessage() {
		return this.detailMessage;
	}

}
