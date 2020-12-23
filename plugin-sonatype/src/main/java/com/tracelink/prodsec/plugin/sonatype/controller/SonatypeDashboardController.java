package com.tracelink.prodsec.plugin.sonatype.controller;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.plugin.sonatype.util.ThreatLevel;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * The Sonatype dashboard controller handles requests to the main landing page
 * for viewing additional statistics and data from the Sonatype Nexus IQ Server.
 *
 * @author mcool
 */
@Controller
@RequestMapping(SonatypePlugin.DASHBOARD_PAGE)
public class SonatypeDashboardController {
	private final ProductsService productsService;
	private final SonatypeAppService appService;

	public SonatypeDashboardController(@Autowired ProductsService productsService,
			@Autowired SonatypeAppService appService) {
		this.productsService = productsService;
		this.appService = appService;
	}

	/**
	 * Render the Sonatype dashboard statistics and main graph.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the
	 *         Sonatype dashboard page
	 */
	@GetMapping("")
	public SynapseModelAndView getDashboard() {
		// Content view
		SynapseModelAndView mv = new SynapseModelAndView("sonatype-dashboard");
		// Small statistics boxes
		List<SonatypeApp> mappedApps = appService.getMappedApps();
		mv.addObject("coveredApps", mappedApps.size());
		mv.addObject("vulnerableApps", mappedApps.stream().filter(SonatypeApp::isVulnerable).count());
		mv.addObject("totalViolations", mappedApps.stream().map(SonatypeApp::getMostRecentMetrics)
				.filter(l -> !l.isEmpty()).mapToLong(l -> l.get(0).getTotalVios()).sum());
		mv.addObject("highViolations", mappedApps.stream().map(SonatypeApp::getMostRecentMetrics)
				.filter(l -> !l.isEmpty()).mapToLong(l -> l.get(0).getHighVios()).sum());
		// Product lines, project filters and projects for the graph dropdown menus
		mv.addObject("productLines", productsService.getAllProductLines());
		mv.addObject("filters", productsService.getAllProjectFilters());
		mv.addObject("projects", productsService.getAllProjects());
		mv.addObject("threatLevels", ThreatLevel.values());
		// Scripts
		mv.addScriptReference("/scripts/util.js");
		mv.addScriptReference("/scripts/violations-bar.js");
		mv.addStyleReference("/styles/sonatype-dashboard.css");
		return mv;
	}
}
