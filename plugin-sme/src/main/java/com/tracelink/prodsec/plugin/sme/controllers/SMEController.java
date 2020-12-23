package com.tracelink.prodsec.plugin.sme.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.plugin.sme.SMEPlugin;
import com.tracelink.prodsec.plugin.sme.service.SMEException;
import com.tracelink.prodsec.plugin.sme.service.SMEService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

/**
 * Controller to manage all interactions with the SME plugin
 * 
 * @author csmith
 *
 */
@Controller
@RequestMapping(SMEPlugin.PAGELINK)
public class SMEController {
	private final SMEService smeService;
	private final ProductsService productsService;

	public SMEController(@Autowired SMEService smeService, @Autowired ProductsService productsService) {
		this.smeService = smeService;
		this.productsService = productsService;
	}

	@GetMapping()
	public SynapseModelAndView smeHome() {
		SynapseModelAndView smav = new SynapseModelAndView("sme/sme");
		smav.addObject("smes", smeService.getAllSMEs());
		smav.addObject("productLines", productsService.getAllProductLines());
		smav.addScriptReference("/scripts/sme.js");
		return smav;
	}

	@PostMapping("/create")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String createSME(@RequestParam String smeName, RedirectAttributes redirectAttributes) {
		try {
			smeService.addNewSME(smeName);
		} catch (SMEException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:" + SMEPlugin.PAGELINK;
	}

	@PostMapping("/setProjects")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String setProjectsForSme(@RequestParam String smeName, @RequestParam(required = false) List<String> projectNames,
			RedirectAttributes redirectAttributes) {
		try {
			smeService.setProjectsForSME(smeName, projectNames);
		} catch (SMEException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:" + SMEPlugin.PAGELINK;
	}

	@PostMapping("/removeProject")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String removeProjectFromSme(@RequestParam String smeName, @RequestParam String projectName,
			RedirectAttributes redirectAttributes) {
		try {
			smeService.removeProjectFromSME(smeName, projectName);
		} catch (SMEException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:" + SMEPlugin.PAGELINK;
	}
}
