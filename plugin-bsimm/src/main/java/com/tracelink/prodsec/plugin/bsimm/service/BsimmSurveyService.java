package com.tracelink.prodsec.plugin.bsimm.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmComparison;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmComparisonFunction;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmComparisonPractice;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmFunction;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmLevel;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmMeasure;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmPractice;
import com.tracelink.prodsec.plugin.bsimm.model.imports.BsimmSurvey;
import com.tracelink.prodsec.plugin.bsimm.model.imports.SurveyImportException;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonPracticeEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmComparisonPracticeRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmComparisonRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmMeasureRepo;
import com.tracelink.prodsec.plugin.bsimm.repo.BsimmSurveyRepo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service to handle Survey Import and Survey CRUD operations.
 *
 * @author csmith
 */
@Service
public class BsimmSurveyService {

	private final BsimmSurveyRepo surveyRepo;
	private final BsimmMeasureRepo measureRepo;
	private final BsimmComparisonRepo comparisonRepo;
	private final BsimmComparisonPracticeRepo practiceRepo;

	public BsimmSurveyService(@Autowired BsimmSurveyRepo surveyRepo, @Autowired BsimmMeasureRepo measureRepo, @Autowired BsimmComparisonRepo comparisonRepo, @Autowired BsimmComparisonPracticeRepo practiceRepo) {
		this.surveyRepo = surveyRepo;
		this.measureRepo = measureRepo;
		this.comparisonRepo = comparisonRepo;
		this.practiceRepo = practiceRepo;
	}

	/////////////
	// Survey Import Functionality
	/////////////
	private BsimmSurvey importFromFile(MultipartFile surveyXML) throws IOException, SurveyImportException {
		BsimmSurvey survey;
		try (InputStream is = surveyXML.getInputStream()) {
			XmlMapper xmlmapper = new XmlMapper();
			survey = xmlmapper.readValue(is, BsimmSurvey.class);
		}
		survey.validate();
		return survey;
	}

	/**
	 * Given a multipart file, create the survey, store the entities and return a
	 * tuple of the Survey Entity and the list of comparisons
	 *
	 * @param surveyXML the xml to be used for importing the survey model
	 * @return a tuple of the saved survey entity and list of comparison objects
	 * @throws IOException           if the import fails due to a file error
	 * @throws SurveyImportException if the survey cannot be imported due to an
	 *                               error
	 */
	public Pair<SurveyEntity, List<SurveyComparisonEntity>> createBsimmSurveyFromFile(MultipartFile surveyXML) throws IOException, SurveyImportException {
		BsimmSurvey survey = importFromFile(surveyXML);

		// create so that the entity exists to be joined on
		SurveyEntity surveyEntity = saveNewSurvey(survey);
		List<MeasureEntity> measures = saveMeasures(survey, surveyEntity);
		List<SurveyComparisonEntity> comparisons = saveComparisons(survey, surveyEntity);
		surveyEntity.setMeasures(measures);
		return Pair.of(surveyEntity, comparisons);
	}

	private SurveyEntity saveNewSurvey(BsimmSurvey survey) throws SurveyImportException {
		if (surveyRepo.findBySurveyName(survey.getSurveyName()) != null) {
			throw new SurveyImportException("A Survey by that name already exists");
		}
		SurveyEntity surveyEntity = new SurveyEntity();
		surveyEntity.setSurveyName(survey.getSurveyName());
		return surveyRepo.save(surveyEntity);
	}

	private List<MeasureEntity> saveMeasures(BsimmSurvey survey, SurveyEntity surveyEntity) {
		// for each measure, add the entity and join to the survey
		List<MeasureEntity> measures = new ArrayList<>();
		for (BsimmFunction function : survey.getFunctions()) {
			for (BsimmPractice practice : function.getPractices()) {
				for (BsimmLevel level : practice.getLevels()) {
					for (BsimmMeasure measure : level.getMeasures()) {
						MeasureEntity measureEntity = new MeasureEntity();
						measureEntity.setOwningSurvey(surveyEntity);
						measureEntity.setFunction(function.getFunctionName());
						measureEntity.setPractice(practice.getPracticeName());
						measureEntity.setLevel(level.getLevelNum());
						measureEntity.setMeasureId(measure.getMeasureId());
						measureEntity.setMeasureTitle(measure.getMeasureTitle());
						measureEntity.setDetail(measure.getDetailMessage());
						measures.add(measureEntity);
					}
				}
			}
		}
		surveyEntity.setMeasures(measures);
		measureRepo.saveAll(measures);
		measureRepo.flush();
		return measures;
	}

	/**
	 * Save the comparisons from the imported survey
	 *
	 * @param survey       the imported survey
	 * @param surveyEntity the imported survey's top level entity
	 * @return a list of comparisons (top level comparisons, not practice scores)
	 */
	public List<SurveyComparisonEntity> saveComparisons(BsimmSurvey survey, SurveyEntity surveyEntity) {
		List<SurveyComparisonEntity> comparisons = new ArrayList<>();
		for (BsimmComparison compare : survey.getComparisons()) {
			SurveyComparisonEntity sce = new SurveyComparisonEntity();
			sce.setOriginalSurvey(surveyEntity);
			sce.setComparisonName(compare.getComparisonTitle());
			sce = comparisonRepo.saveAndFlush(sce);
			List<SurveyComparisonPracticeEntity> practiceEntities = new ArrayList<>();
			for (BsimmComparisonFunction compFunc : compare.getFunctions()) {
				for (BsimmComparisonPractice compPrac : compFunc.getPractices()) {
					SurveyComparisonPracticeEntity scpe = new SurveyComparisonPracticeEntity();
					scpe.setFunctionName(compFunc.getFunctionName());
					scpe.setPracticeName(compPrac.getPracticeName());
					scpe.setScore(compPrac.getValue());
					scpe.setComparison(sce);
					practiceEntities.add(scpe);
				}
			}
			practiceRepo.saveAll(practiceEntities);
			comparisons.add(sce);
		}
		return comparisons;
	}

	public List<SurveyComparisonEntity> getAllComparisons() {
		return comparisonRepo.findAll();
	}

	public SurveyComparisonEntity getComparisonById(Long id) {
		return comparisonRepo.findById(id).orElse(null);
	}

	/**
	 * Deletes the {@link SurveyComparisonEntity} for the given survey, including all associated
	 * {@link SurveyComparisonPracticeEntity} objects.
	 *
	 * @param survey the survey to delete comparisons for
	 */
	public void deleteComparisonForSurvey(SurveyEntity survey) {
		List<SurveyComparisonEntity> comparisons = comparisonRepo.findAllByOriginalSurvey(survey);
		for (SurveyComparisonEntity comparison : comparisons) {
			List<SurveyComparisonPracticeEntity> practices = comparison.getPractices();
			practiceRepo.deleteAll(practices);
		}
		comparisonRepo.deleteAll(comparisons);
	}
	////////////
	// CRUD for Surveys
	////////////

	public List<SurveyEntity> getAllSurveys() {
		return surveyRepo.findAll();
	}

	public SurveyEntity getSurvey(String surveyName) {
		return surveyRepo.findBySurveyName(surveyName);
	}

	/**
	 * Deletes the given survey from the {@link BsimmSurveyRepo}. Also deletes associated
	 * measures and survey comparisons.
	 *
	 * @param survey the survey to delete
	 * @return the survey database object that was deleted
	 */
	public SurveyEntity deleteSurvey(SurveyEntity survey) {
		deleteComparisonForSurvey(survey);
		measureRepo.deleteAll(survey.getMeasures());
		surveyRepo.delete(survey);
		return survey;
	}

}
