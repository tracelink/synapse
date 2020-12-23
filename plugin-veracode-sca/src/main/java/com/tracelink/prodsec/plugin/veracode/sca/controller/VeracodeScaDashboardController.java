package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Veracode SCA dashboard controller handles requests to the main landing page
 * for viewing additional statistics and data from the Veracode SCA server.
 *
 * @author mcool
 */
@Controller
@RequestMapping(VeracodeScaPlugin.DASHBOARD_PAGE)
public class VeracodeScaDashboardController {

	private final ProductsService productsService;
	private final VeracodeScaProjectService projectService;

	public VeracodeScaDashboardController(@Autowired ProductsService productsService,
		@Autowired VeracodeScaProjectService projectService) {
		this.productsService = productsService;
		this.projectService = projectService;
	}

	/**
	 * Returns necessary model objects, content view, and scripts in a
	 * {@link SynapseModelAndView} object to render the Veracode SCA dashboard
	 * statistics and main graph.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the
	 * Veracode SCA dashboard page
	 */
	@GetMapping("")
	public SynapseModelAndView getDashboard() {
		// Content view
		SynapseModelAndView mv = new SynapseModelAndView("veracode-sca-dashboard");
		// Small statistics boxes
		List<VeracodeScaProject> projects = projectService.getProjects();
		mv.addObject("coveredProjects", projects.size());
		mv.addObject("vulnerableProjects",
			projects.stream().filter(VeracodeScaProject::isVulnerable).count());
		mv.addObject("totalIssues", projects.stream()
			.map(VeracodeScaProject::getUnresolvedIssuesForDefaultBranch).mapToLong(List::size)
			.sum());
		mv.addObject("highIssues", projects.stream()
			.map(VeracodeScaProject::getUnresolvedIssuesForDefaultBranch).flatMap(List::stream)
			.filter(i -> i.getSeverityString().equals(VeracodeScaIssue.SEVERITY_HIGH)).count());
		mv.addObject("vulnerableMethods", projects.stream()
			.map(VeracodeScaProject::getUnresolvedIssuesForDefaultBranch).flatMap(List::stream)
			.filter(VeracodeScaIssue::isVulnerableMethod).count());
		// Product lines, project filters and projects for the graph dropdown menus
		mv.addObject("productLines", productsService.getAllProductLines());
		mv.addObject("filters", productsService.getAllProjectFilters());
		mv.addObject("projects", productsService.getAllProjects());
		// Scripts
		mv.addScriptReference("/scripts/veracode/sca/util.js");
		mv.addScriptReference("/scripts/veracode/sca/dashboard.js");
		mv.addStyleReference("/styles/veracode/sca/dashboard.css");
		return mv;
	}
}
