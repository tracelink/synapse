package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Veracode SCA issues controller handles requests to the page for viewing issue statistics per
 * project and supplying links to the Veracode SCA UI.
 *
 * @author mcool
 */
@Controller
@RequestMapping(VeracodeScaPlugin.ISSUES_PAGE)
public class VeracodeScaIssuesController {

	private final ProductsService productsService;
	private final VeracodeScaProjectService projectService;

	public VeracodeScaIssuesController(@Autowired ProductsService productsService,
			@Autowired VeracodeScaProjectService projectService) {
		this.productsService = productsService;
		this.projectService = projectService;
	}

	/**
	 * Returns necessary model objects, content view, and scripts in a {@link SynapseModelAndView}
	 * object to render the Veracode SCA issues tables.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the Veracode SCA issues
	 * page
	 */
	@GetMapping("")
	public SynapseModelAndView getIssues() {
		// Content view
		SynapseModelAndView mv = new SynapseModelAndView("veracode-sca-issues");
		// Product lines, project filters and projects for the graph dropdown menus
		mv.addObject("productLines", productsService.getAllProductLines());
		mv.addObject("mappedProjects", projectService.getMappedProjects());
		mv.addObject("unmappedProjects", projectService.getUnmappedProjects());
		// Scripts
		mv.addScriptReference("/scripts/veracode/sca/issues.js");
		return mv;
	}
}
