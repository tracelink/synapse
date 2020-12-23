package com.tracelink.prodsec.plugin.bsimm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonPracticeEntity;

public interface BsimmComparisonPracticeRepo extends JpaRepository<SurveyComparisonPracticeEntity, Long> {

}
