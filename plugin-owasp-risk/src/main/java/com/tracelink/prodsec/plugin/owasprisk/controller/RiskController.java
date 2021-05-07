package com.tracelink.prodsec.plugin.owasprisk.controller;

import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the BSIMM risk rating page
 */
@Controller
public class RiskController {

	@GetMapping("/risk_rating")
	public ModelAndView getRiskRating(Authentication auth) {
		ModelAndView mav;
		// auth is null if this is the first time someone hits synapse.
		// auth is not authenticated if they have a guest cookie
		if (auth != null && auth.isAuthenticated()) {
			mav = new SynapseModelAndView("risk/risk_rating")
					.addScriptReference("/scripts/risk/riskrating.js")
					.addStyleReference("/styles/risk/riskrating.css");
		} else {
			mav = new ModelAndView("risk/risk_rating_public");
		}
		return mav;
	}

}
