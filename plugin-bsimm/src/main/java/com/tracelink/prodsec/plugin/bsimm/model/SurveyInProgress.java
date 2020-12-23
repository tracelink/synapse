package com.tracelink.prodsec.plugin.bsimm.model;

import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.response.SurveyResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tracks the current status of a survey response as responses are added. this
 * allows responders to come back to surveys.
 *
 * @author csmith
 */
public class SurveyInProgress {

	private final SurveyEntity survey;

	private SurveyResponseEntity surveyResponse;

	private final List<MeasureResponseEntity> responses;

	private final List<MeasureEntity> measures;

	public SurveyInProgress(SurveyEntity survey) {
		this.survey = survey;
		this.measures = survey.getMeasures();
		this.responses = new ArrayList<>(Collections.nCopies(measures.size(), null));
	}

	public SurveyEntity getSurvey() {
		return survey;
	}

	public MeasureEntity getMeasure(int measureNumber) {
		return this.measures.get(measureNumber);
	}

	public boolean isComplete() {
		return responses.stream().noneMatch(Objects::isNull);
	}

	public SurveyResponseEntity getSurveyResponse() {
		return surveyResponse;
	}

	public void setSurveyResponse(SurveyResponseEntity surveyResponse) {
		this.surveyResponse = surveyResponse;
		this.surveyResponse.setMeasureResponses(responses);
	}

	public void setResponse(int measureNumber, MeasureResponseEntity response) {
		this.responses.set(measureNumber, response);
	}

	public MeasureResponseEntity getMeasureResponse(int measureNumber) {
		return this.responses.get(measureNumber);
	}
}
