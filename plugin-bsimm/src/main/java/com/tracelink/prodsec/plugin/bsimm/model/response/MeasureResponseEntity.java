package com.tracelink.prodsec.plugin.bsimm.model.response;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;

/**
 * Response to a Measure Object.
 * 
 * Has 1 to 1 with the {@linkplain MeasureEntity}
 * 
 * Has Many to 1 with the {@linkplain SurveyResponseEntity}
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = BSIMMPlugin.SCHEMA, name = "measure_response")
public class MeasureResponseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "measure_response_id")
	private long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "related_measure")
	private MeasureEntity relatedMeasure;

	@Column(name = "status")
	private String status;

	@Column(name = "responsible")
	private String responsibleParty;

	@Column(name = "response")
	private String responseText;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "survey_response_id")
	private SurveyResponseEntity surveyResponse;

	public long getId() {
		return id;
	}

	public MeasureEntity getRelatedMeasure() {
		return relatedMeasure;
	}

	public void setRelatedMeasure(MeasureEntity relatedMeasure) {
		this.relatedMeasure = relatedMeasure;
	}

	public MeasureResponseStatus getStatus() {
		return MeasureResponseStatus.getMeasureFor(status);
	}

	public void setStatus(MeasureResponseStatus status) {
		this.status = status.getStatusText();
	}

	public String getResponsibleParty() {
		return responsibleParty;
	}

	public void setResponsibleParty(String responsibleParty) {
		this.responsibleParty = responsibleParty;
	}

	public String getResponseText() {
		return responseText;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	public SurveyResponseEntity getSurveyResponse() {
		return surveyResponse;
	}

	public void setSurveyResponse(SurveyResponseEntity surveyResponse) {
		this.surveyResponse = surveyResponse;
	}

	public double getScore() {
		return getStatus().getScore();
	}
}
