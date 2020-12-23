package com.tracelink.prodsec.plugin.veracode.sast.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

/**
 * The mappings controller handles CRUD commands to map a Veracode application
 * against a Synapse Project
 * 
 * @author csmith
 *
 */
@Controller
@RequestMapping(VeracodeSastPlugin.MAPPINGS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeSastMappingsController {
	private final ProductsService productsService;
	private final VeracodeSastAppService appService;

	public VeracodeSastMappingsController(@Autowired ProductsService productsService,
			@Autowired VeracodeSastAppService appService) {
		this.productsService = productsService;
		this.appService = appService;
	}

	@GetMapping("")
	public SynapseModelAndView getMappings() {
		SynapseModelAndView mv = new SynapseModelAndView("veracode-sast-mappings");

		mv.addObject("mappedApps", appService.getMappedApps());
		mv.addObject("synapseProductLines", productsService.getAllProductLines());
		mv.addObject("veracodeApps", appService.getUnmappedApps());

		mv.addScriptReference("/scripts/veracodesast/datatable.js");
		return mv;
	}

	@PostMapping("create")
	public String createMapping(@RequestParam String project, @RequestParam Long app,
			RedirectAttributes redirectAttributes) {
		appService.createMapping(productsService.getProject(project), app);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping.");
		return "redirect:" + VeracodeSastPlugin.MAPPINGS_PAGE;
	}

	@PostMapping("delete")
	public String deleteMapping(@RequestParam Long app, RedirectAttributes redirectAttributes) {
		appService.deleteMapping(app);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Removed mapping.");
		return "redirect:" + VeracodeSastPlugin.MAPPINGS_PAGE;
	}
}
