package com.tracelink.prodsec.plugin.bsimm.service;

import com.tracelink.prodsec.plugin.bsimm.model.SurveyInProgress;
import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseStatus;
import com.tracelink.prodsec.plugin.bsimm.model.response.SurveyResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonPracticeEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmMeasureResponseRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmSurveyResponseRepo;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class BsimmResponseServiceTest {

	@MockBean
	private BsimmSurveyService mockSurveyService;

	@MockBean
	private BsimmSurveyResponseRepo mockSurveyResponseRepo;

	@MockBean
	private BsimmMeasureResponseRepo mockMeasureResponseRepo;

	private BsimmResponseService responseService;

	@Before
	public void setup() {
		this.responseService = new BsimmResponseService(mockSurveyService, mockSurveyResponseRepo,
				mockMeasureResponseRepo);
	}

	@Test
	public void testStartNewSurvey() {
		MockHttpSession session = new MockHttpSession();
		ProductLineModel plm = new ProductLineModel();
		SurveyEntity survey = new SurveyEntity();
		MeasureEntity measure = new MeasureEntity();
		survey.setMeasures(Arrays.asList(measure));
		String author = "Chris";

		SurveyInProgress progress = this.responseService
				.startNewSurvey(session, author, plm, survey);

		Assert.assertEquals(author, progress.getSurveyResponse().getAuthor());
		Assert.assertEquals(survey, progress.getSurveyResponse().getOriginalSurvey());
		Assert.assertEquals(plm, progress.getSurveyResponse().getSurveyTarget());
		Assert.assertEquals(measure, progress.getMeasure(0));
	}

	@Test
	public void testRecordMeasureResponse() throws SurveyException {
		MockHttpSession session = new MockHttpSession();
		ProductLineModel plm = new ProductLineModel();
		SurveyEntity survey = new SurveyEntity();
		MeasureEntity measure = new MeasureEntity();
		survey.setMeasures(Arrays.asList(measure));
		String author = "Chris";

		this.responseService.startNewSurvey(session, author, plm, survey);

		MeasureResponseStatus status = MeasureResponseStatus.COMPLETE;
		String responsible = "Chris";
		String responseText = "responseTextHere";

		SurveyInProgress progress = this.responseService
				.recordMeasureResponse(session, 0, status, responsible,
						responseText);

		MeasureResponseEntity response = progress.getMeasureResponse(0);
		Assert.assertEquals(measure, response.getRelatedMeasure());
		Assert.assertEquals(status, response.getStatus());
		Assert.assertEquals(responsible, response.getResponsibleParty());
		Assert.assertEquals(responseText, response.getResponseText());
		Assert.assertEquals(author, response.getSurveyResponse().getAuthor());
		Assert.assertEquals(survey, response.getSurveyResponse().getOriginalSurvey());
		Assert.assertEquals(plm, response.getSurveyResponse().getSurveyTarget());
	}

	@Test
	public void testRecordMeasureResponseUnknownSession() {
		try {
			this.responseService
					.recordMeasureResponse(new MockHttpSession(), 0, MeasureResponseStatus.COMPLETE,
							"",
							"");
			Assert.fail("Method should have thrown exception");
		} catch (SurveyException e) {
			Assert.assertTrue(e.getMessage().contains("no active surveys"));
		}
	}

	@Test
	public void testRecordMeasureResponseUnknownStatus() {
		try {
			HttpSession session = new MockHttpSession();
			SurveyEntity survey = new SurveyEntity();
			MeasureEntity measure = new MeasureEntity();
			survey.setMeasures(Arrays.asList(measure));
			this.responseService.startNewSurvey(session, "", new ProductLineModel(), survey);
			this.responseService.recordMeasureResponse(session, 0, null, "", "");
			Assert.fail("Method should have thrown exception");
		} catch (SurveyException e) {
			Assert.assertTrue(e.getMessage().contains("Status may not be null"));
		}
	}

	private MeasureResponseEntity makeResponse(String measureId, String measureTitle,
			MeasureResponseStatus status, String function, String practice, int level) {
		MeasureEntity me = new MeasureEntity();
		me.setMeasureId(measureId);
		me.setMeasureTitle(measureTitle);
		me.setFunction(function);
		me.setPractice(practice);
		me.setLevel(level);

		MeasureResponseEntity mre = new MeasureResponseEntity();
		mre.setRelatedMeasure(me);
		mre.setStatus(status);
		return mre;
	}

	@Test
	public void testGetSurveyResult() {
		SurveyResponseEntity entity = new SurveyResponseEntity();
		BDDMockito.when(mockSurveyResponseRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(entity));
		Assert.assertEquals(entity, this.responseService.getSurveyResult(1L));
	}

	@Test
	public void testSaveSurveyResult() throws Exception {
		SurveyEntity survey = new SurveyEntity();
		survey.setMeasures(new ArrayList<>());
		HttpSession session = new MockHttpSession();
		this.responseService.startNewSurvey(session, "", new ProductLineModel(), survey);

		BDDMockito.when(mockSurveyResponseRepo.save(BDDMockito.any())).then(e -> e.getArgument(0));
		SurveyResponseEntity response = this.responseService.saveSurveyResult(session);

		Assert.assertEquals(survey, response.getOriginalSurvey());
		Assert.assertTrue(response.getMeasureResponses().isEmpty());

		try {
			this.responseService.saveSurveyResult(session);
			Assert.fail("Method should have thrown exception");
		} catch (SurveyException e) {
			Assert.assertTrue(e.getMessage().contains("no active surveys"));
		}
	}

	@Test
	public void testSaveSurveyResultIncomplete() throws Exception {
		SurveyEntity survey = new SurveyEntity();
		survey.setMeasures(Arrays.asList(new MeasureEntity()));
		HttpSession session = new MockHttpSession();
		this.responseService.startNewSurvey(session, "", new ProductLineModel(), survey);

		BDDMockito.when(mockSurveyResponseRepo.save(BDDMockito.any())).then(e -> e.getArgument(0));
		try {
			this.responseService.saveSurveyResult(session);
			Assert.fail("Method should have thrown exception");
		} catch (SurveyIncompleteException e) {
			Assert.assertTrue(e.getMessage().contains("not yet complete"));
		}
	}

	@Test
	public void testAmendResponse() throws Exception {
		String measureId = "123";
		MeasureResponseStatus status = MeasureResponseStatus.COMPLETE;
		String responsible = "Chris";
		String responseText = "Done";

		SurveyResponseEntity entity = new SurveyResponseEntity();

		MeasureResponseEntity response = new MeasureResponseEntity();
		MeasureEntity measure = new MeasureEntity();
		measure.setMeasureId(measureId);
		response.setRelatedMeasure(measure);

		MeasureResponseEntity otherResponse = new MeasureResponseEntity();
		MeasureEntity otherMeasure = new MeasureEntity();
		otherMeasure.setMeasureId("other");
		otherResponse.setRelatedMeasure(otherMeasure);

		entity.setMeasureResponses(Arrays.asList(otherResponse, response));
		BDDMockito.when(mockSurveyResponseRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(entity));
		this.responseService
				.amendResponse(1L, measureId, status.getStatusText(), responsible, responseText);

		Assert.assertEquals(status, response.getStatus());
		Assert.assertEquals(responsible, response.getResponsibleParty());
		Assert.assertEquals(responseText, response.getResponseText());
	}

	@Test
	public void testAmendResponseUnknownSurvey() {
		BDDMockito.when(mockSurveyResponseRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.empty());
		try {
			this.responseService.amendResponse(1L, "", "", "", "");
			Assert.fail("Method should have thrown Exception");
		} catch (SurveyException e) {
			Assert.assertTrue(e.getMessage().contains("No Survey Response"));
		}
	}

	@Test
	public void testDeleteSurveyResponse() throws Exception {
		SurveyResponseEntity entity = BDDMockito.mock(SurveyResponseEntity.class);
		BDDMockito.when(mockSurveyResponseRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(entity));
		ArgumentCaptor<SurveyResponseEntity> responseCaptor = ArgumentCaptor
				.forClass(SurveyResponseEntity.class);
		SurveyResponseEntity returnedEntity = this.responseService.deleteSurveyResponse(1L);
		BDDMockito.verify(mockSurveyResponseRepo).delete(responseCaptor.capture());
		Assert.assertEquals(entity, returnedEntity);
		Assert.assertEquals(responseCaptor.getValue(), returnedEntity);
	}

	@Test
	public void testDeleteSurveyResponseUnknownSurvey() {
		BDDMockito.when(mockSurveyResponseRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.empty());
		try {
			this.responseService.deleteSurveyResponse(1L);
			Assert.fail("Method should have thrown Exception");
		} catch (SurveyException e) {
			Assert.assertTrue(e.getMessage().contains("Survey Response does not exist"));
		}
	}

	@Test
	public void testGetLatestResponses() {
		SurveyResponseEntity entity = new SurveyResponseEntity();
		BDDMockito.when(mockSurveyResponseRepo.findTop50ByOrderByDateFiledDesc())
				.thenReturn(Arrays.asList(entity));
		List<SurveyResponseEntity> responses = this.responseService.getLatestResponses();
		Assert.assertTrue(responses.contains(entity));
	}

	@Test
	public void testGenerateResponsesAndComparisons() throws Exception {
		String name = "name";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(name);

		String surveyName = "surveyName";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);

		String measureId = "measure1";
		String measureTitle = "measureTitle";
		MeasureResponseStatus status = MeasureResponseStatus.COMPLETE;
		String function = "function";
		String practice = "practice";
		int level = 1;

		Date dateOne = Date
				.from(LocalDate.of(2019, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault())
						.toInstant());
		SurveyResponseEntity responseOne = new SurveyResponseEntity();
		responseOne.setSurveyTarget(plm);
		responseOne.setOriginalSurvey(survey);
		responseOne.setDateFiled(dateOne);
		responseOne.setMeasureResponses(
				Arrays.asList(
						makeResponse(measureId, measureTitle, status, function, practice, level),
						makeResponse(measureId, measureTitle, status, function, practice, 2)));

		Date dateTwo = Date
				.from(LocalDate.of(2018, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault())
						.toInstant());
		SurveyResponseEntity responseTwo = new SurveyResponseEntity();
		responseTwo.setSurveyTarget(plm);
		responseTwo.setOriginalSurvey(survey);
		responseTwo.setDateFiled(dateTwo);
		responseTwo.setMeasureResponses(
				Arrays.asList(
						makeResponse(measureId, measureTitle, status, function, practice, level)));

		List<SurveyResponseEntity> responses = Arrays.asList(responseOne, responseTwo);

		BDDMockito.when(mockSurveyResponseRepo
				.findBySurveyTargetOrderByDateFiledDesc(BDDMockito.any()))
				.thenReturn(responses);

		String comparisonName = "compareName";
		double comparisonScore = 1.9;

		SurveyComparisonPracticeEntity comparisonPractice = new SurveyComparisonPracticeEntity();
		comparisonPractice.setFunctionName(function);
		comparisonPractice.setPracticeName(practice);
		comparisonPractice.setScore(comparisonScore);

		SurveyComparisonEntity comparison = new SurveyComparisonEntity();
		comparison.setComparisonName(comparisonName);
		comparison.setPractices(Arrays.asList(comparisonPractice));
		comparison.setOriginalSurvey(survey);

		BDDMockito.when(mockSurveyResponseRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(responseOne))
				.thenReturn(Optional.of(responseTwo));
		BDDMockito.when(mockSurveyService.getComparisonById(BDDMockito.anyLong()))
				.thenReturn(comparison);

		String json = this.responseService
				.generateResponsesAndComparisons(Arrays.asList(1L, 2L), Arrays.asList(3L))
				.toString();

		Assert.assertTrue(json.contains(practice));
		Assert.assertTrue(json.contains(function));
		Assert.assertTrue(json.contains(String.valueOf(level)));
		Assert.assertTrue(json.contains(status.getStatusText()));
		Assert.assertTrue(json.contains(measureTitle));
		Assert.assertTrue(json.contains(measureId));
		Assert.assertTrue(json.contains(surveyName));
		Assert.assertTrue(json.contains(name));
		Assert.assertTrue(json.contains(responseOne.getDateString()));
		Assert.assertTrue(json.contains(responseTwo.getDateString()));
		Assert.assertTrue(json.contains(comparisonName));
		Assert.assertTrue(json.contains(String.valueOf(comparisonScore)));
	}

	@Test
	public void testGenerateResponseForProductLineNoResponses() {
		String name = "name";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(name);
		try {
			responseService.generateResponsesAndComparisons(Arrays.asList(1L), Arrays.asList(2L));
			Assert.fail("Method should have thrown exception");
		} catch (SurveyException e) {
			Assert.assertTrue(e.getMessage().contains("No Survey Response"));
			Assert.assertTrue(e.getMessage().contains("No Comparison for"));
		}
	}

	@Test
	public void testGetResponsesForSurvey() {
		SurveyResponseEntity entity = new SurveyResponseEntity();
		BDDMockito.when(mockSurveyResponseRepo.findByOriginalSurveyOrderByIdAsc(BDDMockito.any()))
				.thenReturn(Arrays.asList(entity));
		List<SurveyResponseEntity> responses = this.responseService
				.getResponsesForSurvey(new SurveyEntity());
		Assert.assertTrue(responses.contains(entity));
	}

	@Test
	public void testGetProductLineScorecard() {
		List<Pair<Double, TrafficLight>> params = Arrays.asList(//
				Pair.of(.9, TrafficLight.RED), //
				Pair.of(1.9, TrafficLight.YELLOW), //
				Pair.of(2.9, TrafficLight.GREEN)//
		);
		for (Pair<Double, TrafficLight> param : params) {
			SurveyResponseEntity entity = BDDMockito.mock(SurveyResponseEntity.class);
			BDDMockito.when(entity.getResponseScore()).thenReturn(param.getFirst());
			BDDMockito.when(mockSurveyResponseRepo
					.findBySurveyTargetOrderByDateFiledDesc(BDDMockito.any()))
					.thenReturn(Arrays.asList(entity));
			ScorecardValue scorecard = this.responseService
					.getProductLineScorecard(new ProductLineModel());
			Assert.assertEquals(param.getSecond(), scorecard.getColor());
		}
	}

	@Test
	public void testGetProductLineScorecardEmptyResponses() {
		BDDMockito.when(mockSurveyResponseRepo
				.findBySurveyTargetOrderByDateFiledDesc(BDDMockito.any()))
				.thenReturn(new ArrayList<>());
		ScorecardValue scorecard = this.responseService
				.getProductLineScorecard(new ProductLineModel());
		Assert.assertEquals(TrafficLight.NONE, scorecard.getColor());
	}
}
