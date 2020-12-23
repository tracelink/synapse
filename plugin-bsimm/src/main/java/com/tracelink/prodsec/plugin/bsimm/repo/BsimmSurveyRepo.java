package com.tracelink.prodsec.plugin.bsimm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;

public interface BsimmSurveyRepo extends JpaRepository<SurveyEntity, Long> {

	SurveyEntity findBySurveyName(String surveyName);

}
