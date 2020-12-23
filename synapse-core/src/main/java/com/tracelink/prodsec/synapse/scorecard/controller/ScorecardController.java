package com.tracelink.prodsec.synapse.scorecard.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;

/**
 * Handles url operations for the scorecard and outputting the correctly
 * organized data for the scorecard
 * 
 * @author csmith
 *
 */
@Controller
public class ScorecardController {

	@Autowired
	private ScorecardService scorecardService;

	@Autowired
	private ProductsService productsService;

	private SynapseModelAndView makeScorecardMav() {
		SynapseModelAndView mav = new SynapseModelAndView("scorecard");
		mav.addObject("productLineNames", productsService.getAllProductLines().stream().map(ProductLineModel::getName)
				.collect(Collectors.toList()));
		mav.addObject("filterNames", productsService.getAllProjectFilters().stream().map(ProjectFilterModel::getName)
				.collect(Collectors.toList()));
		mav.addScriptReference("/scripts/scorecard.js");
		mav.addStyleReference("/styles/scorecard.css");
		return mav;
	}

	@GetMapping("/")
	public SynapseModelAndView scorecardHome(@RequestParam(required = false) String filterType,
			@RequestParam(required = false) String name, RedirectAttributes redirectAttributes) {
		SynapseModelAndView mav = makeScorecardMav();

		// standard scorecard of orgs
		if (filterType == null && name == null) {
			standardScorecard(mav);
		} else {
			filteredScorecard(mav, filterType, name, redirectAttributes);
		}
		return mav;
	}

	private void standardScorecard(SynapseModelAndView mav) {
		mav.addObject("scorecard", scorecardService.getTopLevelScorecard());
		mav.addObject("scorecardType", "Top Level View");
	}

	private void filteredScorecard(SynapseModelAndView mav, String filterType, String name,
			RedirectAttributes redirectAttributes) {

		// error conditions for filtering misconfigs
		if (StringUtils.isEmpty(filterType)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "You must include a filter type");
			mav.setViewName("redirect:/");
			return;
		}
		if (StringUtils.isEmpty(name)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "You must include a filter name");
			mav.setViewName("redirect:/");
			return;
		}

		// do the correct filtering
		try {
			switch (filterType.toLowerCase()) {
			case "productline":
				mav.addObject("scorecard", scorecardService.getScorecardForProductLine(name));
				mav.addObject("scorecardType", "Product Line View: " + name);
				break;
			case "filter":
				mav.addObject("scorecard", scorecardService.getScorecardForFilter(name));
				mav.addObject("scorecardType", "Filter View: " + name);
				break;
			case "project":
				mav.addObject("scorecard", scorecardService.getScorecardForProject(name));
				mav.addObject("scorecardType", "Project View: " + name);
				break;
			default:
				redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Unknown filter type");
				mav.setViewName("redirect:/");
				break;
			}
		} catch (ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Could not get scorecard due to error: " + e.getMessage());
			mav.setViewName("redirect:/");
		}
	}

}
