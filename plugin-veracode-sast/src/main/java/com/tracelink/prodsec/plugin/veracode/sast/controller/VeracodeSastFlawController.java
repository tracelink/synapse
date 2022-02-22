package com.tracelink.prodsec.plugin.veracode.sast.controller;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastReportService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The flaw controller is used to show the status of the latest report from each
 * app as well as the detailed flaw information for a single report
 *
 * @author csmith
 */
@Controller
@RequestMapping(VeracodeSastPlugin.FLAWS_PAGE)
@PreAuthorize("hasAuthority('" + VeracodeSastPlugin.FLAWS_VIEWER_PRIVILEGE + "')")
public class VeracodeSastFlawController {

	private final VeracodeSastAppService appService;
	private final VeracodeSastReportService reportService;
	private final ProductsService productsService;
	private static final String UNMAPPED_KEY = "Unmapped Apps";

	public VeracodeSastFlawController(@Autowired VeracodeSastAppService appService,
			@Autowired VeracodeSastReportService reportService,
			@Autowired ProductsService productsService) {
		this.appService = appService;
		this.reportService = reportService;
		this.productsService = productsService;
	}

	@GetMapping("")
	public SynapseModelAndView getFlaws() {
		SynapseModelAndView smav = new SynapseModelAndView("veracode-sast-flaws");

		smav.addObject("mappedProjects", appService.getMappedApps());
		smav.addObject("unmappedProjects", appService.getUnmappedApps());
		smav.addObject("productLines", productsService.getAllProductLines());

		smav.addScriptReference("/scripts/veracodesast/flaws.js");

		return smav;
	}
}
