package com.tracelink.prodsec.plugin.bsimm.service;

import com.tracelink.prodsec.plugin.bsimm.model.imports.SurveyImportException;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmComparisonPracticeRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmComparisonRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmMeasureRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmSurveyRepo;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

@RunWith(SpringRunner.class)
public class BsimmSurveyServiceTest {

	@MockBean
	private BsimmComparisonRepo mockComparisonRepo;

	@MockBean
	private BsimmSurveyRepo mockSurveyRepo;

	@MockBean
	private BsimmMeasureRepo mockMeasureRepo;

	@MockBean
	private BsimmComparisonPracticeRepo mockPracticeRepo;

	private BsimmSurveyService surveyService;

	@Before
	public void setup() {
		this.surveyService = new BsimmSurveyService(mockSurveyRepo, mockMeasureRepo,
				mockComparisonRepo,
				mockPracticeRepo);
	}

	@Test
	public void importSurveyTest() throws SurveyImportException {
		BDDMockito.when(mockSurveyRepo.save(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);
		Pair<SurveyEntity, List<SurveyComparisonEntity>> result = doImportTest(
				"/xmlmodel_test/surveymodel.xml");
		BDDMockito.verify(mockSurveyRepo).save(BDDMockito.any());
		BDDMockito.verify(mockPracticeRepo).saveAll(BDDMockito.any());
		BDDMockito.verify(mockComparisonRepo).saveAndFlush(BDDMockito.any());
		BDDMockito.verify(mockMeasureRepo).saveAll(BDDMockito.any());

		Assert.assertEquals("Example BSIMM Survey Name Model", result.getFirst().getSurveyName());
		Assert.assertEquals(3, result.getFirst().getMeasures().size());
		Assert.assertEquals(1, result.getSecond().size());
	}

	@Test
	public void importSurveyFailuresTest() {
		BDDMockito.when(mockSurveyRepo.save(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);

		Object[][] badinputs = new Object[][]{ //
				{"/xmlmodel_test/surveymodel_badnumfunctions.xml",
						"don't match number of Functions"}, //
				{"/xmlmodel_test/surveymodel_badnumpractice.xml",
						"don't match number of Practices"}, //
				{"/xmlmodel_test/surveymodel_mismatchfunctions.xml",
						"No matching Comparison Function"}, //
				{"/xmlmodel_test/surveymodel_mismatchpractice.xml",
						"No matching Comparison Practice"} //
		};
		for (Object[] badinput : badinputs) {
			System.out.println(badinput[0]);
			try {
				doImportTest(String.valueOf(badinput[0]));
				Assert.fail("Expected a failure message containing \"" + badinput[1]
						+ "\", but didn't get one");
			} catch (SurveyImportException ex) {
				Assert.assertTrue(ex.getMessage().contains(String.valueOf(badinput[1])));
			}
		}
	}

	@Test
	public void testSurveyExists() {
		BDDMockito.when(mockSurveyRepo.findBySurveyName(BDDMockito.anyString()))
				.thenReturn(new SurveyEntity());
		try {
			doImportTest("/xmlmodel_test/surveymodel.xml");
			Assert.fail("Should have thrown SIE");
		} catch (SurveyImportException e) {
			Assert.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	private Pair<SurveyEntity, List<SurveyComparisonEntity>> doImportTest(String resourceName)
			throws SurveyImportException {
		String filename = Paths.get(resourceName).getFileName().toString();
		Pair<SurveyEntity, List<SurveyComparisonEntity>> result = null;
		try (InputStream is = new FileInputStream(
				ResourceUtils.getFile(this.getClass().getResource(resourceName)))) {
			MultipartFile mockFileUpload = new MockMultipartFile(filename, is);
			result = surveyService.createBsimmSurveyFromFile(mockFileUpload);
		} catch (IOException ex) {
			Assert.fail("Cannot do import");
		}
		return result;
	}

	@Test
	public void getComparisonForSurveyTest() {
		SurveyComparisonEntity comparison = new SurveyComparisonEntity();
		BDDMockito.when(mockComparisonRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(comparison));
		SurveyComparisonEntity retComparison = surveyService.getComparisonById(1L);
		Assert.assertEquals(comparison, retComparison);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteComparisonForSurveyTest() {
		List<SurveyComparisonEntity> comparisons = Arrays
				.asList(BDDMockito.mock(SurveyComparisonEntity.class));
		BDDMockito.when(mockComparisonRepo.findAllByOriginalSurvey(BDDMockito.any()))
				.thenReturn(comparisons);
		surveyService.deleteComparisonForSurvey(new SurveyEntity());
		ArgumentCaptor<List<SurveyComparisonEntity>> compCaptor = ArgumentCaptor
				.forClass(List.class);
		BDDMockito.verify(mockComparisonRepo).deleteAll(compCaptor.capture());
		Assert.assertEquals(comparisons, compCaptor.getValue());
	}

	@Test
	public void getAllSurveysTest() {
		List<SurveyEntity> surveys = new ArrayList<>();
		BDDMockito.when(mockSurveyRepo.findAll()).thenReturn(surveys);
		Assert.assertEquals(surveys, surveyService.getAllSurveys());
	}

	@Test
	public void getSurveyTest() {
		SurveyEntity survey = new SurveyEntity();
		BDDMockito.when(mockSurveyRepo.findBySurveyName(BDDMockito.anyString())).thenReturn(survey);
		Assert.assertEquals(survey, surveyService.getSurvey("foo"));
	}

	@Test
	public void deleteSurveyTest() {
		SurveyEntity survey = new SurveyEntity();
		BDDMockito.when(mockSurveyRepo.findBySurveyName(BDDMockito.anyString())).thenReturn(survey);
		Assert.assertEquals(survey, surveyService.deleteSurvey(survey));
	}
}
