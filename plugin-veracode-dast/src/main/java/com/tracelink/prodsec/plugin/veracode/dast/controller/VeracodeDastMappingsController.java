package com.tracelink.prodsec.plugin.veracode.dast.controller;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The mappings controller handles CRUD commands to map a Veracode application against a Synapse
 * Product Line
 *
 * @author csmith
 */
@Controller
@RequestMapping(VeracodeDastPlugin.MAPPINGS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeDastMappingsController {

	private final ProductsService productsService;
	private final VeracodeDastAppService appService;

	public VeracodeDastMappingsController(@Autowired ProductsService productsService,
			@Autowired VeracodeDastAppService appService) {
		this.productsService = productsService;
		this.appService = appService;
	}

	@GetMapping("")
	public SynapseModelAndView getMappings() {
		SynapseModelAndView mv = new SynapseModelAndView("veracode-dast-mappings");

		mv.addObject("mappedApps", appService.getMappedApps());
		mv.addObject("synapseProductLines", productsService.getAllProductLines());
		mv.addObject("veracodeApps", appService.getUnmappedApps());

		mv.addScriptReference("/scripts/veracodedast/datatable.js");
		return mv;
	}

	@PostMapping("create")
	public String createMapping(@RequestParam String productLine, @RequestParam String app,
			RedirectAttributes redirectAttributes) {
		appService.createMapping(productsService.getProductLine(productLine), app);
		redirectAttributes
				.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping.");
		return "redirect:" + VeracodeDastPlugin.MAPPINGS_PAGE;
	}

	@PostMapping("delete")
	public String deleteMapping(@RequestParam String app, RedirectAttributes redirectAttributes) {
		appService.deleteMapping(app);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Removed mapping.");
		return "redirect:" + VeracodeDastPlugin.MAPPINGS_PAGE;
	}
}
