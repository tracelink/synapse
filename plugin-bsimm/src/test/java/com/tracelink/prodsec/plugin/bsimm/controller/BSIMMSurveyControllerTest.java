package com.tracelink.prodsec.plugin.bsimm.controller;

import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;
import com.tracelink.prodsec.plugin.bsimm.model.SurveyInProgress;
import com.tracelink.prodsec.plugin.bsimm.model.imports.SurveyImportException;
import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseStatus;
import com.tracelink.prodsec.plugin.bsimm.model.response.SurveyResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.plugin.bsimm.service.BsimmResponseService;
import com.tracelink.prodsec.plugin.bsimm.service.BsimmSurveyService;
import com.tracelink.prodsec.plugin.bsimm.service.SurveyException;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class BSIMMSurveyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BsimmResponseService mockBsimmResponseService;

	@MockBean
	private BsimmSurveyService mockBsimmSurveyService;

	@MockBean
	private ProductsService mockProductsService;

	@Test
	@WithMockUser
	public void testOverview() throws Exception {
		String firstName = "firstName";
		String secondName = "secondName";
		String thirdName = "thirdName";
		ProductLineModel first = new ProductLineModel();
		first.setName(firstName);
		ProductLineModel second = new ProductLineModel();
		second.setName(secondName);
		ProductLineModel third = new ProductLineModel();
		third.setName(thirdName);
		BDDMockito.when(mockProductsService.getAllProductLines())
				.thenReturn(Arrays.asList(first, second, third));

		Double responseOneScore = 0.0;
		Double responseTwoScore = 2.0;
		SurveyResponseEntity responseOne = BDDMockito.mock(SurveyResponseEntity.class);
		BDDMockito.when(responseOne.getResponseScore()).thenReturn(responseOneScore);
		SurveyResponseEntity responseTwo = BDDMockito.mock(SurveyResponseEntity.class);
		BDDMockito.when(responseTwo.getResponseScore()).thenReturn(responseTwoScore);

		BDDMockito.when(mockBsimmResponseService.getResponsesForProductLine(first))
				.thenReturn(Arrays.asList(responseOne));
		BDDMockito.when(mockBsimmResponseService.getResponsesForProductLine(second))
				.thenReturn(Arrays.asList(responseTwo));
		// product with no responses (shouldn't add to metrics
		BDDMockito.when(mockBsimmResponseService.getResponsesForProductLine(third))
				.thenReturn(new ArrayList<>());

		BDDMockito.when(mockBsimmResponseService.getLatestResponses())
				.thenReturn(Arrays.asList(responseOne, responseTwo));
		SurveyComparisonEntity comparison = BDDMockito.mock(SurveyComparisonEntity.class);

		BDDMockito.when(mockBsimmSurveyService.getAllComparisons())
				.thenReturn(Arrays.asList(comparison));

		mockMvc.perform(MockMvcRequestBuilders.get(BSIMMPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLinesReviewed", Matchers.is(2)))
				.andExpect(
						MockMvcResultMatchers.model().attribute("avgMaturity", Matchers.is("1.00")))
				.andExpect(MockMvcResultMatchers.model().attribute("lowestMaturity", firstName))
				.andExpect(MockMvcResultMatchers.model().attribute("highestMaturity", secondName))
				.andExpect(MockMvcResultMatchers.model().attribute("responses",
						Matchers.hasItems(responseOne, responseTwo)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("comparisons", Matchers.hasItem(comparison)))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("scripts", Matchers.hasItem("/scripts/bsimm-radar.js")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("bsimm/overview")));
	}

	@Test
	@WithMockUser
	public void testViewResponses() throws Exception {
		String plmName = "Product Name";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));

		String surveyName = "Survey Name";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);
		BDDMockito.when(mockBsimmSurveyService.getAllSurveys()).thenReturn(Arrays.asList(survey));

		SurveyResponseEntity response = BDDMockito.mock(SurveyResponseEntity.class);
		BDDMockito.when(response.getSurveyTarget()).thenReturn(plm);
		BDDMockito.when(response.getResponseScore()).thenReturn(0.0);
		BDDMockito.when(response.getOriginalSurvey()).thenReturn(survey);
		BDDMockito.when(mockBsimmResponseService.getLatestResponses())
				.thenReturn(Arrays.asList(response));

		mockMvc.perform(MockMvcRequestBuilders.get(BSIMMPlugin.PAGELINK + "/survey"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLines", Matchers.contains(plmName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("surveyNames", Matchers.contains(surveyName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("surveyResponses", Matchers.contains(response)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("bsimm/survey")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testImportSurvey() throws Exception {
		String surveyName = "surveyName";

		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);
		survey.setMeasures(Arrays.asList(new MeasureEntity()));

		List<SurveyComparisonEntity> comparisons = Arrays.asList(new SurveyComparisonEntity());

		MockMultipartFile file = new MockMultipartFile("surveyfile", "testdata".getBytes());
		BDDMockito.when(mockBsimmSurveyService.createBsimmSurveyFromFile(BDDMockito.any()))
				.thenReturn(Pair.of(survey, comparisons));

		mockMvc.perform(
				MockMvcRequestBuilders.multipart(BSIMMPlugin.PAGELINK + "/importSurvey").file(file)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, Matchers.allOf(//
								Matchers.containsString(surveyName), //
								Matchers.containsString("with 1"), //
								Matchers.containsString("1 comparisons"))))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testImportSurveyFail() throws Exception {
		BDDMockito.given(mockBsimmSurveyService.createBsimmSurveyFromFile(BDDMockito.any()))
				.willThrow(SurveyImportException.class);
		MockMultipartFile file = new MockMultipartFile("surveyfile", "testdata".getBytes());
		mockMvc.perform(
				MockMvcRequestBuilders.multipart(BSIMMPlugin.PAGELINK + "/importSurvey").file(file)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Failed to import")))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteSurvey() throws Exception {
		String surveyName = "SurveyName";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);
		BDDMockito.when(mockBsimmSurveyService.getSurvey(BDDMockito.anyString()))
				.thenReturn(survey);
		BDDMockito.when(mockBsimmResponseService.getResponsesForSurvey(BDDMockito.any()))
				.thenReturn(new ArrayList<>());
		BDDMockito.when(mockBsimmSurveyService.deleteSurvey(BDDMockito.any()))
				.thenAnswer(e -> e.getArgument(0));

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/deleteSurvey")
				.param("surveyName", surveyName).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.allOf(Matchers.containsString("Deleted Survey"),
										Matchers.containsString(surveyName))))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteSurveyFailNoSurvey() throws Exception {
		String surveyName = "SurveyName";

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/deleteSurvey")
				.param("surveyName", surveyName).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Survey does not exist")))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteSurveyFailHasResponses() throws Exception {

		String surveyName = "SurveyName";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);
		BDDMockito.when(mockBsimmSurveyService.getSurvey(BDDMockito.anyString()))
				.thenReturn(survey);
		BDDMockito.when(mockBsimmResponseService.getResponsesForSurvey(BDDMockito.any()))
				.thenReturn(Arrays.asList(new SurveyResponseEntity()));
		BDDMockito.when(mockBsimmSurveyService.deleteSurvey(BDDMockito.any()))
				.thenAnswer(e -> e.getArgument(0));

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/deleteSurvey")
				.param("surveyName", surveyName).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"Cannot delete survey with known responses")))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteSurveyResponse() throws Exception {
		String surveyName = "surveyName";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);
		SurveyResponseEntity response = new SurveyResponseEntity();
		response.setOriginalSurvey(survey);
		BDDMockito.when(mockBsimmResponseService.deleteSurveyResponse(BDDMockito.anyLong()))
				.thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/deleteSurveyResponse")
				.param("surveyResponseId", "1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.allOf(Matchers.containsString(
										"Deleted Survey Response For Survey"),
										Matchers.containsString(surveyName))))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteSurveyResponseFail() throws Exception {
		String message = "exceptionMessage";
		SurveyException exception = new SurveyException(message);
		BDDMockito.given(mockBsimmResponseService.deleteSurveyResponse(BDDMockito.anyLong()))
				.willThrow(exception);
		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/deleteSurveyResponse")
				.param("surveyResponseId", "1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(message)))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testNewResponse() throws Exception {
		String plmName = "plmName";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);
		BDDMockito.when(mockProductsService.getProductLine(BDDMockito.anyString())).thenReturn(plm);

		String surveyName = "surveyName";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);
		MeasureEntity measure = new MeasureEntity();
		measure.setOwningSurvey(survey);
		survey.setMeasures(Arrays.asList(measure));
		BDDMockito.when(mockBsimmSurveyService.getSurvey(BDDMockito.anyString()))
				.thenReturn(survey);

		SurveyInProgress surveyInProgress = new SurveyInProgress(survey);
		BDDMockito.when(mockBsimmResponseService
				.startNewSurvey(BDDMockito.any(), BDDMockito.anyString(),
						BDDMockito.any(), BDDMockito.any())).thenReturn(surveyInProgress);

		mockMvc.perform(
				MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/newResponse")
						.param("productLine", plmName)
						.param("surveyName", surveyName)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.model().attribute("measure", Matchers.is(measure)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("surveyName", Matchers.is(surveyName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLine", Matchers.is(plmName)))
				.andExpect(MockMvcResultMatchers.model().attribute("statuses",
						Matchers.arrayContaining(MeasureResponseStatus.values())))
				.andExpect(MockMvcResultMatchers.model().attribute("measureNumber", Matchers.is(0)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("bsimm/questionnaire")));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testNewResponseFailBadProductLine() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/newResponse")
				.param("productLine", "product").param("surveyName", "survey")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Product Line does not exist")))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testNewResponseFailNoSurvey() throws Exception {
		BDDMockito.when(mockProductsService.getProductLine(BDDMockito.anyString()))
				.thenReturn(new ProductLineModel());

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/newResponse")
				.param("productLine", "product").param("surveyName", "survey")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Survey does not exist")))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testResponse() throws Exception {
		String surveyName = "surveyName";
		SurveyEntity survey = new SurveyEntity();
		survey.setSurveyName(surveyName);

		MeasureEntity measure = new MeasureEntity();
		measure.setOwningSurvey(survey);

		MeasureEntity measureTwo = new MeasureEntity();
		measure.setOwningSurvey(survey);

		survey.setMeasures(Arrays.asList(measure, measureTwo));
		BDDMockito.when(mockBsimmSurveyService.getSurvey(BDDMockito.anyString()))
				.thenReturn(survey);

		SurveyInProgress surveyInProgress = new SurveyInProgress(survey);

		String productLineName = "plmName";
		ProductLineModel productLine = new ProductLineModel();
		productLine.setName(productLineName);

		SurveyResponseEntity surveyResponse = new SurveyResponseEntity();
		surveyResponse.setSurveyTarget(productLine);

		surveyInProgress.setSurveyResponse(surveyResponse);
		BDDMockito.when(mockBsimmResponseService
				.recordMeasureResponse(BDDMockito.any(), BDDMockito.anyInt(),
						BDDMockito.any(), BDDMockito.any(), BDDMockito.any()))
				.thenReturn(surveyInProgress);

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/questionnaire")
				.param("measureNumber", "0").param("status", "status")
				.param("responsible", "responsible")
				.param("response", "response").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.model().attribute("measure", Matchers.is(measureTwo)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("existingResponse", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("surveyName", Matchers.is(surveyName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLine", Matchers.is(productLineName)))
				.andExpect(MockMvcResultMatchers.model().attribute("statuses",
						Matchers.arrayContaining(MeasureResponseStatus.values())))
				.andExpect(MockMvcResultMatchers.model().attribute("measureNumber", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("bsimm/questionnaire")));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testResponseFail() throws Exception {
		String message = "exceptionMessage";
		SurveyException execption = new SurveyException(message);
		BDDMockito.given(mockBsimmResponseService
				.recordMeasureResponse(BDDMockito.any(), BDDMockito.anyInt(),
						BDDMockito.any(), BDDMockito.any(), BDDMockito.any())).willThrow(execption);

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/questionnaire")
				.param("measureNumber", "0").param("status", "status")
				.param("responsible", "responsible")
				.param("response", "response").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.is(message)))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testResponseComplete() throws Exception {
		SurveyInProgress prog = BDDMockito.mock(SurveyInProgress.class);
		BDDMockito.when(prog.isComplete()).thenReturn(true);
		BDDMockito.when(mockBsimmResponseService
				.recordMeasureResponse(BDDMockito.any(), BDDMockito.anyInt(),
						BDDMockito.any(), BDDMockito.any(), BDDMockito.any())).thenReturn(prog);
		long id = 2L;
		SurveyResponseEntity response = BDDMockito.mock(SurveyResponseEntity.class);
		BDDMockito.when(response.getId()).thenReturn(id);
		BDDMockito.when(mockBsimmResponseService.saveSurveyResult(BDDMockito.any()))
				.thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/questionnaire")
				.param("measureNumber", "0").param("status", "status")
				.param("responsible", "responsible")
				.param("response", "response").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers
						.redirectedUrl(
								BSIMMPlugin.PAGELINK + "/survey/review?surveyResponseId=" + id));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testResponseCompleteFail() throws Exception {
		SurveyInProgress prog = BDDMockito.mock(SurveyInProgress.class);
		BDDMockito.when(prog.isComplete()).thenReturn(true);

		BDDMockito.when(mockBsimmResponseService
				.recordMeasureResponse(BDDMockito.any(), BDDMockito.anyInt(),
						BDDMockito.any(), BDDMockito.any(), BDDMockito.any())).thenReturn(prog);
		String message = "exceptionMessage";
		SurveyException exception = new SurveyException(message);
		BDDMockito.given(mockBsimmResponseService.saveSurveyResult(BDDMockito.any()))
				.willThrow(exception);

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/questionnaire")
				.param("measureNumber", "0").param("status", "status")
				.param("responsible", "responsible")
				.param("response", "response").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.is(message)))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));
	}

	@Test
	@WithMockUser
	public void testReview() throws Exception {
		long responseId = 1L;
		String plmName = "plmName";
		ProductLineModel surveyTarget = new ProductLineModel();
		surveyTarget.setName(plmName);

		String functionName = "functionName";
		int level = 1;
		String practiceName = "practiceName";
		MeasureResponseStatus status = MeasureResponseStatus.COMPLETE;
		String surveyName = "surveyName";

		SurveyEntity originalSurvey = new SurveyEntity();
		originalSurvey.setSurveyName(surveyName);

		MeasureEntity measure = new MeasureEntity();
		measure.setFunction(functionName);
		measure.setLevel(level);
		measure.setPractice(practiceName);

		originalSurvey.setMeasures(Arrays.asList(measure));

		SurveyResponseEntity sre = new SurveyResponseEntity();
		sre.setOriginalSurvey(originalSurvey);
		sre.setSurveyTarget(surveyTarget);

		MeasureResponseEntity response = new MeasureResponseEntity();
		response.setRelatedMeasure(measure);
		response.setStatus(status);
		response.setSurveyResponse(sre);

		List<MeasureResponseEntity> measures = Arrays.asList(response);
		sre.setMeasureResponses(measures);

		BDDMockito.when(mockBsimmResponseService.getSurveyResult(BDDMockito.anyLong()))
				.thenReturn(sre);

		mockMvc.perform(MockMvcRequestBuilders.get(BSIMMPlugin.PAGELINK + "/survey/review")
				.param("surveyResponseId", String.valueOf(responseId))
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("surveyResponseId", Matchers.is(responseId)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("results", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("surveyName", Matchers.is(surveyName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLineName", Matchers.is(plmName)))
				.andExpect(MockMvcResultMatchers.model().attribute("score", Matchers.is("1.00")))
				.andExpect(MockMvcResultMatchers.model().attribute("statuses",
						Matchers.arrayContaining(MeasureResponseStatus.values())))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts",
						Matchers.hasItem("/scripts/bsimm-review.js")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("bsimm/review")));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testOverwriteReview() throws Exception {
		String id = "1";
		mockMvc.perform(
				MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/review")
						.param("surveyResponseId", id)
						.param("measure", "measure").param("status", "status")
						.param("responsible", "responsible")
						.param("response", "response")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers
						.redirectedUrl(
								BSIMMPlugin.PAGELINK + "/survey/review?surveyResponseId=" + id));
		BDDMockito.verify(mockBsimmResponseService)
				.amendResponse(BDDMockito.anyLong(), BDDMockito.anyString(),
						BDDMockito.anyString(), BDDMockito.anyString(), BDDMockito.anyString());
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testOverwriteReviewFail() throws Exception {
		String id = "1";
		String message = "exceptionMessage";
		SurveyException exception = new SurveyException(message);
		BDDMockito.doThrow(exception).when(mockBsimmResponseService)
				.amendResponse(BDDMockito.anyLong(),
						BDDMockito.anyString(), BDDMockito.anyString(), BDDMockito.anyString(),
						BDDMockito.anyString());
		mockMvc.perform(
				MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/survey/review")
						.param("surveyResponseId", id)
						.param("measure", "measure").param("status", "status")
						.param("responsible", "responsible")
						.param("response", "response")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.is(message)))
				.andExpect(MockMvcResultMatchers
						.redirectedUrl(
								BSIMMPlugin.PAGELINK + "/survey/review?surveyResponseId=" + id));
	}

	@Test
	@WithMockUser(authorities = BSIMMPlugin.PRIV)
	public void testCancelSurvey() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/cancelSurvey")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("Sucessfully cancelled")))
				.andExpect(MockMvcResultMatchers.redirectedUrl(BSIMMPlugin.PAGELINK + "/survey"));

		BDDMockito.verify(mockBsimmResponseService).deleteSurveyInProgress(BDDMockito.any());
	}
}
