package com.tracelink.prodsec.plugin.bsimm.repo;

import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to store BSIMM surveys.
 *
 * @author csmith
 */
public interface BsimmSurveyRepo extends JpaRepository<SurveyEntity, Long> {

	SurveyEntity findBySurveyName(String surveyName);

}
