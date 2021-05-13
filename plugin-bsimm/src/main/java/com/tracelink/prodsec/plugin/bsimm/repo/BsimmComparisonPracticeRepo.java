package com.tracelink.prodsec.plugin.bsimm.repo;

import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyComparisonPracticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to store comparisons for BSIMM practices.
 *
 * @author csmith
 */
public interface BsimmComparisonPracticeRepo extends JpaRepository<SurveyComparisonPracticeEntity, Long> {

}
