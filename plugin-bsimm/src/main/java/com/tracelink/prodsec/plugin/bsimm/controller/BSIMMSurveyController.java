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
import com.tracelink.prodsec.plugin.bsimm.service.SurveyIncompleteException;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller to view BSIMM overview page and review survey responses.
 *
 * @author csmith
 */
@Controller
@RequestMapping(BSIMMPlugin.PAGELINK)
public class BSIMMSurveyController {

	private static final Logger LOG = LoggerFactory.getLogger(BSIMMSurveyController.class);

	private final BsimmResponseService bsimmResponseService;
	private final BsimmSurveyService bsimmSurveyService;
	private final ProductsService productsService;

	public BSIMMSurveyController(@Autowired BsimmResponseService bsimmResponseService, @Autowired BsimmSurveyService bsimmSurveyService, @Autowired ProductsService productsService) {
		this.bsimmResponseService = bsimmResponseService;
		this.bsimmSurveyService = bsimmSurveyService;
		this.productsService = productsService;
	}

	@GetMapping
	public SynapseModelAndView bsimmOverview() {
		SynapseModelAndView smav = new SynapseModelAndView("bsimm/overview");
		smav.addScriptReference("/scripts/bsimm-radar.js");

		List<ProductLineModel> productLines = productsService.getAllProductLines();

		int hasResponse = 0;
		double totalMaturity = 0.0;
		// need to get the highest and lowest product line maturity
		// also getting the overall average maturity
		Pair<String, Double> lowest = null;
		Pair<String, Double> highest = null;
		for (ProductLineModel productLine : productLines) {
			List<SurveyResponseEntity> resps = bsimmResponseService.getResponsesForProductLine(productLine);
			if (!resps.isEmpty()) {
				SurveyResponseEntity resp = resps.get(0);
				double maturity = resp.getResponseScore();
				totalMaturity += maturity;
				hasResponse++;
				if (lowest == null || lowest.getSecond() > maturity) {
					lowest = Pair.of(productLine.getName(), maturity);
				}
				if (highest == null || highest.getSecond() < maturity) {
					highest = Pair.of(productLine.getName(), maturity);
				}
			}
		}

		smav.addObject("productLinesReviewed", hasResponse);
		smav.addObject("avgMaturity", hasResponse == 0 ? "N/A" : String.format("%.2f", totalMaturity / (double) hasResponse));
		smav.addObject("lowestMaturity", lowest == null ? "N/A" : lowest.getFirst());
		smav.addObject("highestMaturity", highest == null ? "N/A" : highest.getFirst());
		smav.addObject("responses", bsimmResponseService.getLatestResponses());

		smav.addObject("comparisons", bsimmSurveyService.getAllComparisons());
		return smav;
	}

	@GetMapping("/survey")
	public SynapseModelAndView viewResponses() {
		SynapseModelAndView smav = new SynapseModelAndView("bsimm/survey");

		List<ProductLineModel> productLines = productsService.getAllProductLines();
		smav.addObject("productLines", productLines.stream().map(ProductLineModel::getName).collect(Collectors.toList()));
		smav.addObject("surveyNames", bsimmSurveyService.getAllSurveys().stream().map(SurveyEntity::getSurveyName).collect(Collectors.toList()));
		smav.addObject("surveyResponses", bsimmResponseService.getLatestResponses());
		smav.addScriptReference("/scripts/bsimm-survey.js");
		return smav;
	}

	@PostMapping("/importSurvey")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String importSurvey(@RequestParam MultipartFile surveyfile, RedirectAttributes redirectAttributes) {
		Pair<SurveyEntity, List<SurveyComparisonEntity>> surveyImportResult;
		try {
			surveyImportResult = bsimmSurveyService.createBsimmSurveyFromFile(surveyfile);
			SurveyEntity survey = surveyImportResult.getFirst();
			int comparisons = surveyImportResult.getSecond().size();
			String name = survey.getSurveyName();
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Successfully created new Survey: " + name + " with " + survey.getMeasures().size() + " measures and " + comparisons + " comparisons");
		} catch (IOException | SurveyImportException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Failed to import survey due to exception: " + e.getMessage());
			LOG.error("Exception while importing survey", e);
		}

		return "redirect:" + BSIMMPlugin.PAGELINK + "/survey";
	}

	@PostMapping("/deleteSurvey")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String deleteSurvey(@RequestParam String surveyName, RedirectAttributes redirectAttributes) {
		try {
			SurveyEntity survey = bsimmSurveyService.getSurvey(surveyName);
			if (survey == null) {
				throw new SurveyException("Survey does not exist");
			}
			List<SurveyResponseEntity> responses = bsimmResponseService.getResponsesForSurvey(survey);
			if (responses.size() > 0) {
				throw new SurveyException("Cannot delete survey with known responses. " + responses.size() + " responses recorded.");
			}
			survey = bsimmSurveyService.deleteSurvey(survey);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Deleted Survey \"" + survey.getSurveyName() + "\"");
		} catch (SurveyException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:" + BSIMMPlugin.PAGELINK + "/survey";
	}

	@PostMapping("/deleteSurveyResponse")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String deleteSurveyResponse(@RequestParam long surveyResponseId, RedirectAttributes redirectAttributes) {
		try {
			SurveyResponseEntity survey = bsimmResponseService.deleteSurveyResponse(surveyResponseId);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Deleted Survey Response For Survey \"" + survey.getOriginalSurvey().getSurveyName() + "\"");
		} catch (SurveyException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:" + BSIMMPlugin.PAGELINK + "/survey";
	}

	@PostMapping("/copySurveyResponse")
	@PreAuthorize("hasAuthority('" + BSIMMPlugin.PRIV + "')")
	public SynapseModelAndView copyResponse(@RequestParam String productLine, @RequestParam long surveyResponseId, Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
		SynapseModelAndView smav = new SynapseModelAndView("bsimm/questionnaire");
		ProductLineModel plm = productsService.getProductLine(productLine);
		if (plm == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Product Line does not exist");
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}

		SurveyResponseEntity copied = bsimmResponseService.getSurveyResult(surveyResponseId);
		if (copied == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Could not find responses to copy");
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}

		// start a new progress, iterate through all of the answers in order, and save
		bsimmResponseService.startNewSurvey(session, principal.getName(), plm, copied.getOriginalSurvey());
		try {
			int i = 0;
			for (MeasureResponseEntity copiedResponse : copied.getMeasureResponses()) {
				bsimmResponseService.recordMeasureResponse(session, i, copiedResponse.getStatus(), copiedResponse.getResponsibleParty(), copiedResponse.getResponseText());
				i++;
			}
		} catch (SurveyException e) {
			bsimmResponseService.deleteSurveyInProgress(session);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Could not copy old responses due to error: " + e.getMessage());
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}

		// assume complete (caught in exceptions)
		SurveyResponseEntity resp;
		try {
			resp = bsimmResponseService.saveSurveyResult(session);
		} catch (SurveyException | SurveyIncompleteException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}
		smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey/review?surveyResponseId=" + resp.getId());
		return smav;
	}

	@PostMapping("/survey/newResponse")
	@PreAuthorize("hasAuthority('" + BSIMMPlugin.PRIV + "')")
	public SynapseModelAndView startNewResponse(@RequestParam String productLine, @RequestParam String surveyName, Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
		SynapseModelAndView smav = new SynapseModelAndView("bsimm/questionnaire");
		ProductLineModel plm = productsService.getProductLine(productLine);
		if (plm == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Product Line does not exist");
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}
		SurveyEntity survey = bsimmSurveyService.getSurvey(surveyName);
		if (survey == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Survey does not exist");
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}

		SurveyInProgress surveyProgress = bsimmResponseService.startNewSurvey(session, principal.getName(), plm, survey);
		MeasureEntity measureEntity = surveyProgress.getMeasure(0);
		smav.addObject("measure", measureEntity);
		smav.addObject("surveyName", measureEntity.getOwningSurvey().getSurveyName());
		smav.addObject("productLine", plm.getName());
		smav.addObject("statuses", MeasureResponseStatus.values());
		smav.addObject("measureNumber", 0);

		return smav;
	}

	@PostMapping("/survey/questionnaire")
	@PreAuthorize("hasAuthority('" + BSIMMPlugin.PRIV + "')")
	public SynapseModelAndView response(@RequestParam int measureNumber, @RequestParam String status, @RequestParam String responsible, @RequestParam String response, HttpSession session, RedirectAttributes redirectAttributes) {
		SynapseModelAndView smav = new SynapseModelAndView("bsimm/questionnaire");

		SurveyInProgress surveyProgress;
		try {
			surveyProgress = bsimmResponseService.recordMeasureResponse(session, measureNumber, MeasureResponseStatus.getMeasureFor(status), responsible, response);
		} catch (SurveyException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
			return smav;
		}

		if (surveyProgress.isComplete()) {
			SurveyResponseEntity resp;
			try {
				resp = bsimmResponseService.saveSurveyResult(session);
			} catch (SurveyException | SurveyIncompleteException e) {
				redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
				smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey");
				return smav;
			}
			smav.setViewName("redirect:" + BSIMMPlugin.PAGELINK + "/survey/review?surveyResponseId=" + resp.getId());
			return smav;
		}

		measureNumber++;
		MeasureEntity nextMeasureEntity = surveyProgress.getMeasure(measureNumber);
		smav.addObject("measure", nextMeasureEntity);
		smav.addObject("existingResponse", surveyProgress.getMeasureResponse(measureNumber));
		smav.addObject("surveyName", surveyProgress.getSurvey().getSurveyName());
		smav.addObject("productLine", surveyProgress.getSurveyResponse().getSurveyTarget().getName());
		smav.addObject("statuses", MeasureResponseStatus.values());
		smav.addObject("measureNumber", measureNumber);
		return smav;
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/survey/review")
	public SynapseModelAndView review(@RequestParam long surveyResponseId, RedirectAttributes redirectAttributes) {
		SynapseModelAndView smav = new SynapseModelAndView("bsimm/review");
		smav.addScriptReference("/scripts/bsimm-review.js");
		SurveyResponseEntity result = bsimmResponseService.getSurveyResult(surveyResponseId);
		Map<String, Object> functionMap = new LinkedHashMap<>();
		for (MeasureResponseEntity response : result.getMeasureResponses()) {
			MeasureEntity measure = response.getRelatedMeasure();

			String funcName = measure.getFunctionName();
			Map<String, Object> practiceMap = (Map<String, Object>) functionMap.getOrDefault(funcName, new LinkedHashMap<String, Object>());

			String pracName = measure.getPracticeName();
			List<MeasureResponseEntity> responseList = (List<MeasureResponseEntity>) practiceMap.getOrDefault(pracName, new ArrayList<MeasureResponseEntity>());

			responseList.add(response);

			practiceMap.put(pracName, responseList);
			functionMap.put(funcName, practiceMap);
		}
		smav.addObject("surveyResponseId", surveyResponseId);
		smav.addObject("results", functionMap);
		smav.addObject("surveyName", result.getOriginalSurvey().getSurveyName());
		smav.addObject("productLineName", result.getSurveyTarget().getName());
		smav.addObject("score", String.format("%.2f", result.getResponseScore()));
		smav.addObject("statuses", MeasureResponseStatus.values());
		return smav;
	}

	@PostMapping("/survey/review")
	@PreAuthorize("hasAuthority('" + BSIMMPlugin.PRIV + "')")
	public String overwriteReview(@RequestParam long surveyResponseId, @RequestParam String measure, @RequestParam String status, @RequestParam String responsible, @RequestParam String response, RedirectAttributes redirectAttributes) {
		MeasureResponseEntity responseEntity = null;
		try {
			responseEntity = bsimmResponseService.amendResponse(surveyResponseId, measure, status, responsible, response);
		} catch (SurveyException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:" + BSIMMPlugin.PAGELINK + "/survey/review?surveyResponseId=" + surveyResponseId + (responseEntity == null ? "" : "#response-" + responseEntity.getId());
	}

	@PostMapping("/cancelSurvey")
	@PreAuthorize("hasAuthority('" + BSIMMPlugin.PRIV + "')")
	public String cancelSurvey(HttpSession session, RedirectAttributes redirectAttributes) {
		bsimmResponseService.deleteSurveyInProgress(session);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Sucessfully cancelled the current survey");
		return "redirect:" + BSIMMPlugin.PAGELINK + "/survey";
	}

}
