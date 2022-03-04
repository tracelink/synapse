package com.tracelink.prodsec.plugin.veracode.dast.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

/**
 * The flaw controller is used to show the status of the latest report from each app as well as the
 * detailed flaw information for a single report
 *
 * @author csmith
 */
@Controller
@RequestMapping(VeracodeDastPlugin.FLAWS_PAGE)
@PreAuthorize("hasAuthority('" + VeracodeDastPlugin.FLAWS_VIEWER_PRIVILEGE + "')")
public class VeracodeDastReportController {

	private final VeracodeDastAppService appService;
	private final ProductsService productsService;

	public VeracodeDastReportController(@Autowired VeracodeDastAppService appService,
			@Autowired ProductsService productsService) {
		this.appService = appService;
		this.productsService = productsService;
	}

	@GetMapping("")
	public SynapseModelAndView getFlaws() {
		SynapseModelAndView smav = new SynapseModelAndView("veracode-dast-flaws");

		smav.addObject("mappedApps", appService.getMappedApps());
		smav.addObject("unmappedApps", appService.getUnmappedApps());
		smav.addObject("productLines", productsService.getAllProductLines());

		smav.addScriptReference("/scripts/veracodedast/flaws.js");

		return smav;
	}

}
