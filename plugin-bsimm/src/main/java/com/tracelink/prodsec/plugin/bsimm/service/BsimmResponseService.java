package com.tracelink.prodsec.plugin.bsimm.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to track, show, and store responses to surveys
 *
 * @author csmith
 */
@Service
public class BsimmResponseService {

	/*
	 * WeakHashMap is used to allow the progress to fall out of scope if a survey is
	 * started, but not finished. If the HTTPSession dies, the progress will be
	 * cleaned here after garbage collection
	 */
	private final Map<HttpSession, SurveyInProgress> surveyProgress = new WeakHashMap<>();

	private final BsimmSurveyService surveyService;
	private final BsimmSurveyResponseRepo surveyResponseRepo;
	private final BsimmMeasureResponseRepo measureResponseRepo;

	public BsimmResponseService(@Autowired BsimmSurveyService surveyService, @Autowired BsimmSurveyResponseRepo surveyResponseRepo, @Autowired BsimmMeasureResponseRepo measureResponseRepo) {
		this.surveyService = surveyService;
		this.surveyResponseRepo = surveyResponseRepo;
		this.measureResponseRepo = measureResponseRepo;
	}

	////////////
	// Survey Responses Functionality
	////////////

	/**
	 * Begin a survey for a user session/author against a given product line and
	 * survey
	 *
	 * @param userSession the current active session
	 * @param author      the author (session owner)
	 * @param productLine the product line to respond against
	 * @param survey      the survey to respond to
	 * @return the {@linkplain SurveyInProgress} to track this work
	 */
	public SurveyInProgress startNewSurvey(HttpSession userSession, String author, ProductLineModel productLine, SurveyEntity survey) {
		SurveyInProgress prog = new SurveyInProgress(survey);
		SurveyResponseEntity surveyResp = new SurveyResponseEntity();
		surveyResp.setDateFiled(new Date());
		surveyResp.setSurveyTarget(productLine);
		surveyResp.setOriginalSurvey(survey);
		surveyResp.setAuthor(author);
		prog.setSurveyResponse(surveyResp);
		surveyProgress.put(userSession, prog);
		return prog;
	}

	/**
	 * Records a response for the current session
	 *
	 * @param userSession   the current active session
	 * @param measureNumber the survey measure this response is for
	 * @param status        the response's measure status
	 * @param responsible   the response's responsible party
	 * @param responseText  the response's response comment/text field
	 * @return the {@linkplain SurveyInProgress} to track this work
	 * @throws SurveyException if the user doesn't have a survey or the status is
	 *                         unknown
	 */
	public SurveyInProgress recordMeasureResponse(HttpSession userSession, int measureNumber, MeasureResponseStatus status, String responsible, String responseText) throws SurveyException {

		SurveyInProgress prog = surveyProgress.get(userSession);
		if (prog == null) {
			throw new SurveyException("User has no active surveys");
		}
		if (status == null) {
			throw new SurveyException("Status may not be null");
		}
		MeasureEntity currentMeasure = prog.getMeasure(measureNumber);
		MeasureResponseEntity response = new MeasureResponseEntity();
		response.setRelatedMeasure(currentMeasure);
		response.setStatus(status);
		response.setResponsibleParty(responsible);
		response.setResponseText(responseText);
		response.setSurveyResponse(prog.getSurveyResponse());
		prog.setResponse(measureNumber, response);
		return prog;
	}

	public SurveyResponseEntity getSurveyResult(long surveyResponseId) {
		Optional<SurveyResponseEntity> entity = this.surveyResponseRepo.findById(surveyResponseId);
		return entity.orElse(null);
	}

	/**
	 * Save the current session's survey only if it is complete
	 *
	 * @param userSession the current active session
	 * @return the saved response entity
	 * @throws SurveyException           if the session doesn't have an active
	 *                                   survey
	 * @throws SurveyIncompleteException if the survey isn't complete yet
	 */
	public SurveyResponseEntity saveSurveyResult(HttpSession userSession) throws SurveyException, SurveyIncompleteException {
		SurveyInProgress prog = surveyProgress.get(userSession);
		if (prog == null) {
			throw new SurveyException("User has no active surveys");
		}
		if (!prog.isComplete()) {
			throw new SurveyIncompleteException("Survey is not yet complete");
		}
		SurveyResponseEntity resp = prog.getSurveyResponse();
		resp = surveyResponseRepo.save(resp);
		measureResponseRepo.saveAll(resp.getMeasureResponses());
		measureResponseRepo.flush();
		surveyResponseRepo.flush();
		deleteSurveyInProgress(userSession);
		return resp;
	}

	/**
	 * Edit a response after it's been saved
	 *
	 * @param surveyResponseId the survey response id
	 * @param measureId        the measureID to modify
	 * @param status           the new status
	 * @param responsible      the new responsible party
	 * @param response         the new text response
	 * @return the edited measure response
	 * @throws SurveyException if there is no survey response for the given ID
	 */
	public MeasureResponseEntity amendResponse(long surveyResponseId, String measureId, String status, String responsible, String response) throws SurveyException {
		SurveyResponseEntity survey = getSurveyResult(surveyResponseId);
		if (survey == null) {
			throw new SurveyException("No Survey Response for that id");
		}
		MeasureResponseEntity correctResponseEntity = null;
		for (MeasureResponseEntity responseEntity : survey.getMeasureResponses()) {
			if (responseEntity.getRelatedMeasure().getMeasureId().equals(measureId)) {
				correctResponseEntity = responseEntity;
				MeasureResponseStatus measureResponseStatus = MeasureResponseStatus.getMeasureFor(status);
				if (measureResponseStatus == null) {
					throw new SurveyException("Invalid measure response status");
				}
				responseEntity.setStatus(measureResponseStatus);
				responseEntity.setResponsibleParty(responsible);
				responseEntity.setResponseText(response);
				measureResponseRepo.save(responseEntity);
				surveyResponseRepo.save(survey);
				break;
			}
		}
		return correctResponseEntity;
	}

	/**
	 * End the current session's in-progress survey
	 *
	 * @param userSession the user's session to use to end the survey
	 */
	public void deleteSurveyInProgress(HttpSession userSession) {
		surveyProgress.remove(userSession);
	}

	public List<SurveyResponseEntity> getResponsesForProductLine(ProductLineModel productLine) {
		return surveyResponseRepo.findBySurveyTargetOrderByDateFiledDesc(productLine);
	}

	/**
	 * remove the given survey response from the database
	 *
	 * @param id the id of the response
	 * @return the deleted survey response
	 * @throws SurveyException if the survey response doesn't exist
	 */
	public SurveyResponseEntity deleteSurveyResponse(long id) throws SurveyException {
		SurveyResponseEntity survey = getSurveyResult(id);
		if (survey == null) {
			throw new SurveyException("Survey Response does not exist");
		}
		measureResponseRepo.deleteAll(survey.getMeasureResponses());
		surveyResponseRepo.delete(survey);
		return survey;
	}

	public List<SurveyResponseEntity> getLatestResponses() {
		return surveyResponseRepo.findTop50ByOrderByDateFiledDesc();
	}

	/**
	 * Creates a JSON containing the given product line's current survey response
	 * and optionally the previous (single) response and optionally the survey's
	 * comparison scores.
	 * <p>
	 * The model is:
	 *
	 * <pre>
	 * {
	 *   "productLine": "{productLineName}",
	 *   "responses": [response_objects,...],
	 *   "comparisons": [comparison_objects,...]
	 * }
	 * </pre>
	 * <p>
	 * Where response_objects is:
	 * </p>
	 *
	 * <pre>
	 * {
	 *   "title":"",
	 *   "functions": [{
	 *     "functionName":"",
	 *     "practices":[{
	 *       "practiceName":"",
	 *       "levels":[{
	 *         "levelNum":"",
	 *         "measures":[{
	 *           "measureId":"",
	 *           "measureTitle":"",
	 *           "measureStatus":"",
	 *           "measureScore": 0.0
	 *         },
	 *         ...
	 *         ]
	 *       },
	 *       ...
	 *       ]
	 *     },
	 *     ...
	 *     ]
	 *   },
	 *   ...
	 *   ]
	 * }
	 * </pre>
	 * <p>
	 * And comparison_objects is:
	 * </p>
	 *
	 * <pre>
	 * {
	 *   "comparisonTitle": "",
	 *   "functions": [{
	 *     "functionName":"",
	 *     "practices":[{
	 *       "practiceName":"",
	 *       "comparisonScore": 0.0
	 *     },
	 *     ...
	 *     ]
	 *   },
	 *   ...
	 *   ]
	 * }
	 * </pre>
	 *
	 * @param responses   the survey responses ids
	 * @param comparisons the survey comparison data ids
	 * @return a JSON object with score data for the given product line
	 * @throws SurveyException if the given product line doesn't have any responses
	 */
	public JsonObject generateResponsesAndComparisons(List<Long> responses, List<Long> comparisons) throws SurveyException {
		JsonObject json = new JsonObject();

		JsonArray responseArr = new JsonArray();
		json.add("responses", responseArr);

		JsonArray compareArr = new JsonArray();
		json.add("comparisons", compareArr);

		List<String> errors = new ArrayList<>();

		for (Long id : responses) {
			SurveyResponseEntity response = getResponseById(id);
			if (response == null) {
				errors.add("No Survey Response for id: " + id);
			} else {
				generateMeasureResponsesForResponse(response, responseArr);
			}
		}
		for (Long id : comparisons) {
			SurveyComparisonEntity comparison = surveyService.getComparisonById(id);
			if (comparison == null) {
				errors.add("No Comparison for id: " + id);
			} else {
				generateComparisonsForResponse(comparison, compareArr);
			}
		}
		if (!errors.isEmpty()) {
			throw new SurveyException(Strings.join(errors, ','));
		}
		return json;
	}

	private void generateComparisonsForResponse(SurveyComparisonEntity comparison, JsonArray compareArr) {
		JsonObject comparisonObj = getObjectFromOrAddToJsonArray(compareArr, "comparisonTitle", comparison.getTitle());
		List<SurveyComparisonPracticeEntity> practices = comparison.getPractices();
		for (SurveyComparisonPracticeEntity practiceEntity : practices) {
			JsonArray functionsArr = getArrayFromOrAddToJsonObject(comparisonObj, "functions");

			JsonObject function = getObjectFromOrAddToJsonArray(functionsArr, "functionName", practiceEntity.getFunctionName());

			JsonArray practiceArr = getArrayFromOrAddToJsonObject(function, "practices");

			JsonObject practice = getObjectFromOrAddToJsonArray(practiceArr, "practiceName", practiceEntity.getPracticeName());
			practice.addProperty("comparisonScore", practiceEntity.getScore());
		}
	}

	private void generateMeasureResponsesForResponse(SurveyResponseEntity response, JsonArray responseArr) {
		JsonObject responseObj = new JsonObject();
		responseObj.addProperty("title", response.getTitle());

		responseArr.add(responseObj);

		List<MeasureResponseEntity> measures = response.getMeasureResponses();

		JsonArray measuresData = new JsonArray();
		for (MeasureResponseEntity measure : measures) {
			JsonObject measureData = new JsonObject();
			MeasureEntity originalMeasure = measure.getRelatedMeasure();
			measureData.addProperty("measureId", originalMeasure.getMeasureId());
			measureData.addProperty("measureTitle", originalMeasure.getMeasureTitle());
			measureData.addProperty("measureStatus", measure.getStatus().getStatusText());
			measureData.addProperty("measureScore", measure.getScore());
			measuresData.add(measureData);

			JsonArray functionsArr = getArrayFromOrAddToJsonObject(responseObj, "functions");

			JsonObject function = getObjectFromOrAddToJsonArray(functionsArr, "functionName", originalMeasure.getFunctionName());

			JsonArray practiceArr = getArrayFromOrAddToJsonObject(function, "practices");

			JsonObject practice = getObjectFromOrAddToJsonArray(practiceArr, "practiceName", originalMeasure.getPracticeName());

			JsonArray levelsArr = getArrayFromOrAddToJsonObject(practice, "levels");

			JsonObject level = getObjectFromOrAddToJsonArray(levelsArr, "levelNum", Integer.toString(originalMeasure.getLevel()));

			JsonArray measuresArr = getArrayFromOrAddToJsonObject(level, "measures");
			measuresArr.add(measureData);
		}
	}

	private static JsonObject getObjectFromOrAddToJsonArray(JsonArray array, String key, String value) {
		JsonObject target = null;
		for (JsonElement elem : array) {
			if (elem.isJsonObject() && ((JsonObject) elem).has(key) && ((JsonObject) elem).get(key).getAsString().equals(value)) {
				target = (JsonObject) elem;
			}
		}
		if (target == null) {
			JsonObject newObj = new JsonObject();
			newObj.addProperty(key, value);
			array.add(newObj);
			target = newObj;
		}
		return target;
	}

	private static JsonArray getArrayFromOrAddToJsonObject(JsonObject obj, String key) {
		JsonArray target = (JsonArray) obj.get(key);
		if (target == null) {
			target = new JsonArray();
			obj.add(key, target);
		}
		return target;
	}

	public SurveyResponseEntity getResponseById(Long id) {
		return surveyResponseRepo.findById(id).orElse(null);
	}

	public List<SurveyResponseEntity> getResponsesForSurvey(SurveyEntity survey) {
		return surveyResponseRepo.findByOriginalSurveyOrderByIdAsc(survey);
	}
	//////////////
	// Callbacks
	//////////////

	/**
	 * Generate the ScorecardValue for this ProductLine.
	 *
	 * <pre>
	 * Scorecard Value is the score is out of 3.0
	 * Green if 2.0 or greater
	 * Yellow if 1.0-2.0 exclusive
	 * Red if 0.0-1.0 exclusive
	 * </pre>
	 *
	 * @param productLine the product line to generate the score for
	 * @return a scorecard value for this ProductLine
	 */
	public ScorecardValue getProductLineScorecard(ProductLineModel productLine) {
		List<SurveyResponseEntity> responses = getResponsesForProductLine(productLine);
		if (responses.isEmpty()) {
			return new ScorecardValue("N/A", TrafficLight.NONE);
		}
		SurveyResponseEntity response = responses.get(0);
		double score = response.getResponseScore();
		TrafficLight color;
		if (score < 1.0) {
			color = TrafficLight.RED;
		} else if (score < 2.0) {
			color = TrafficLight.YELLOW;
		} else {
			color = TrafficLight.GREEN;
		}
		return new ScorecardValue(String.format("%.2f/3.00", score), color);
	}

}
