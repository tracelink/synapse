package com.tracelink.prodsec.plugin.veracode.dast.controller;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastReportService;
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
 * The flaw controller is used to show the status of the latest report from each app as well as the
 * detailed flaw information for a single report
 *
 * @author csmith
 */
@Controller
@RequestMapping(VeracodeDastPlugin.FLAWS_PAGE)
@PreAuthorize("hasAuthority('" + VeracodeDastPlugin.FLAWS_VIEWER_PRIVILEGE + "')")
public class VeracodeDastFlawController {

	private final VeracodeDastAppService appService;
	private final VeracodeDastReportService reportService;
	private final ProductsService productsService;

	public VeracodeDastFlawController(@Autowired VeracodeDastAppService appService,
			@Autowired VeracodeDastReportService reportService,
			@Autowired ProductsService productsService) {
		this.appService = appService;
		this.reportService = reportService;
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

	@GetMapping("report")
	public SynapseModelAndView showReport(@RequestParam long reportId,
			RedirectAttributes redirectAttributes) {
		SynapseModelAndView smav = new SynapseModelAndView("veracode-dast-report");
		Optional<VeracodeDastReportModel> reportOpt = reportService.getReportById(reportId);
		if (reportOpt.isPresent()) {
			smav.addObject("report", reportOpt.get());
			smav.addScriptReference("/scripts/veracodedast/datatable.js");
		} else {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Unknown report");
			smav.setViewName("redirect:" + VeracodeDastPlugin.FLAWS_PAGE);
		}
		return smav;
	}
}
