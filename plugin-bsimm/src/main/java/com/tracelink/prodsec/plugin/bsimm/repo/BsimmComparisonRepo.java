package com.tracelink.prodsec.plugin.bsimm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;

public interface BsimmComparisonRepo extends JpaRepository<SurveyComparisonEntity, Long> {

	/**
	 * Find the Comparisons based on the survey they compare against
	 * 
	 * @param originalSurvey the Survey a comparison compares against
	 * @return a list of all survey comparisons that compare against the given
	 *         survey
	 */
	List<SurveyComparisonEntity> findAllByOriginalSurvey(SurveyEntity originalSurvey);

}
