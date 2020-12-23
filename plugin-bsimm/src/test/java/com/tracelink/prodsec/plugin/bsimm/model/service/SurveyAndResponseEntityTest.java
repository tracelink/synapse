package com.tracelink.prodsec.plugin.bsimm.model.service;

import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseStatus;
import com.tracelink.prodsec.plugin.bsimm.model.response.SurveyResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class SurveyAndResponseEntityTest {

	@Test
	public void testResponseFunctionality() {
		String author = "Chris";
		int year = 2019;
		int month = 4;
		int day = 5;
		Date dateFiled = Date
				.from(LocalDate.of(year, month, day).atStartOfDay().atZone(ZoneId.systemDefault())
						.toInstant());
		ProductLineModel surveyTarget = new ProductLineModel();
		String detailMessage = "detailMessage";
		String functionName = "functionName";
		int level = 1;
		String measureId = "measureId";
		String measureTitle = "measureTitle";
		String practiceName = "practiceName";
		String responseText = "responseText";
		String responsibleParty = "responsibleParty";
		MeasureResponseStatus status = MeasureResponseStatus.COMPLETE;
		String surveyName = "surveyName";

		SurveyEntity originalSurvey = new SurveyEntity();
		originalSurvey.setSurveyName(surveyName);

		MeasureEntity measure = new MeasureEntity();
		measure.setDetail(detailMessage);
		measure.setFunction(functionName);
		measure.setLevel(level);
		measure.setMeasureId(measureId);
		measure.setMeasureTitle(measureTitle);
		measure.setOwningSurvey(originalSurvey);
		measure.setPractice(practiceName);

		originalSurvey.setMeasures(Arrays.asList(measure));

		SurveyResponseEntity sre = new SurveyResponseEntity();
		sre.setAuthor(author);
		sre.setDateFiled(dateFiled);
		sre.setOriginalSurvey(originalSurvey);
		sre.setSurveyTarget(surveyTarget);

		MeasureResponseEntity response = new MeasureResponseEntity();
		response.setRelatedMeasure(measure);
		response.setResponseText(responseText);
		response.setResponsibleParty(responsibleParty);
		response.setStatus(status);
		response.setSurveyResponse(sre);

		List<MeasureResponseEntity> measures = Arrays.asList(response);
		sre.setMeasureResponses(measures);

		Assert.assertEquals(surveyName, originalSurvey.getSurveyName());
		Assert.assertTrue(originalSurvey.getMeasures().contains(measure));
		Assert.assertEquals(detailMessage, measure.getDetailMessage());
		Assert.assertEquals(functionName, measure.getFunctionName());
		Assert.assertEquals(measureId, measure.getMeasureId());
		Assert.assertEquals(measureTitle, measure.getMeasureTitle());
		Assert.assertEquals(practiceName, measure.getPracticeName());
		Assert.assertEquals(level, measure.getLevel());
		Assert.assertEquals(originalSurvey, measure.getOwningSurvey());
		Assert.assertEquals(author, sre.getAuthor());
		Assert.assertTrue(sre.getDateString().contains(String.valueOf(year)));
		Assert.assertTrue(sre.getDateString().contains("Apr"));
		Assert.assertTrue(sre.getDateString().contains("05"));
		Assert.assertEquals(dateFiled, sre.getDateFiled());
		Assert.assertTrue(sre.getMeasureResponses().contains(response));
		Assert.assertEquals(originalSurvey, sre.getOriginalSurvey());
		Assert.assertEquals(status.getScore(), sre.getResponseScore(), 0.001);
		Assert.assertEquals(surveyTarget, sre.getSurveyTarget());
		Assert.assertEquals(responseText, response.getResponseText());
		Assert.assertEquals(responsibleParty, response.getResponsibleParty());
		Assert.assertEquals(measure, response.getRelatedMeasure());
		Assert.assertEquals(status.getScore(), response.getScore(), 0.001);
		Assert.assertEquals(status, response.getStatus());
		Assert.assertEquals(sre, response.getSurveyResponse());
	}
}
