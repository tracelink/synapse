package com.tracelink.prodsec.plugin.bsimm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelink.prodsec.plugin.bsimm.model.response.SurveyResponseEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;

public interface BsimmSurveyResponseRepo extends JpaRepository<SurveyResponseEntity, Long> {

	/**
	 * Find Responses for a Product Line sorted recent to oldest
	 * 
	 * @param productLine the product line to search against
	 * @return a List of responses for the given product line
	 */
	List<SurveyResponseEntity> findBySurveyTargetOrderByDateFiledDesc(ProductLineModel productLine);

	/**
	 * Find Responses for a given Survey
	 * 
	 * @param survey the original survey
	 * @return a list of responses for that survey
	 */
	List<SurveyResponseEntity> findByOriginalSurveyOrderByIdAsc(SurveyEntity survey);

	/**
	 * Find the most recent 50 responses
	 * 
	 * @return a list of the most recent 0-50 responses
	 */
	List<SurveyResponseEntity> findTop50ByOrderByDateFiledDesc();

}
