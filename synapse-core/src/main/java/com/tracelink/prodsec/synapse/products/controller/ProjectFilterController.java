package com.tracelink.prodsec.synapse.products.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.BadProductNameException;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles CRUD operations for Project Filters. Does not handle operations for
 * the underlying project(s)
 *
 * @author csmith
 */
@Controller
public class ProjectFilterController {

	private final ProductsService productsService;

	public ProjectFilterController(@Autowired ProductsService productsService) {
		this.productsService = productsService;
	}

	private SynapseModelAndView projectFilterMAV() {
		SynapseModelAndView mav = new SynapseModelAndView("projectfilter");
		mav.addScriptReference("/scripts/filter.js");
		return mav;
	}

	@GetMapping("/projectfilter")
	public SynapseModelAndView projectFilterHome() {
		SynapseModelAndView mav = projectFilterMAV();
		mav.addObject("filters", productsService.getAllProjectFilters());
		return mav;
	}

	@GetMapping("/projectfilter/{filter}")
	public SynapseModelAndView filterView(@PathVariable String filter) {
		SynapseModelAndView mav = projectFilterMAV();
		mav.addObject("projectNames", productsService.getAllProjects());
		mav.addObject("filters", productsService.getAllProjectFilters());
		mav.addObject("filter", productsService.getProjectFilter(filter));
		return mav;
	}

	/////////////////
	// Create
	/////////////////

	@PostMapping("/projectfilter/createfilter")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String createFilter(@RequestParam String filterName,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.createProjectFilter(filterName);
		} catch (BadProductNameException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/projectfilter/" + filterName;
	}

	/////////////////
	// Rename and Update
	/////////////////
	@PostMapping("/projectfilter/{filter}/rename")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String moveProjectFilter(@PathVariable String filter, @RequestParam String filterName,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.renameProjectFilter(filter, filterName);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					filter + " renamed to " + filterName);
		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/projectfilter/" + filterName;
	}

	@PostMapping("/projectfilter/{filter}/setprojects")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String setProjectsForFilter(@PathVariable String filter,
			@RequestParam(required = false) List<String> projectNames,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.setProjectsForFilter(projectNames, filter);
		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/projectfilter/" + filter;
	}

	@PostMapping("/projectfilter/{filter}/removeproject")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String removeProjectFromFilter(@PathVariable String filter,
			@RequestParam String projectName,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.removeProjectFromFilter(projectName, filter);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Project " + projectName + " removed from " + filter);
		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/projectfilter/" + filter;
	}

	/////////////////
	// Delete
	/////////////////

	@PostMapping("/projectfilter/{filter}/delete")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String deleteFilter(@PathVariable String filter, RedirectAttributes redirectAttributes) {
		try {
			productsService.deleteProjectFilter(filter);
		} catch (ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/projectfilter";
	}
}
