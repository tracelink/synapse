package com.tracelink.prodsec.plugin.veracode.dast.controller;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

/**
 * The Dashboard controller shows the primary dashboard of statistics for this
 * plugin
 * 
 * @author csmith
 *
 */
@Controller
@RequestMapping(VeracodeDastPlugin.DASHBOARD_PAGE)
public class VeracodeDastDashboardController {
	private final ProductsService productsService;
	private final VeracodeDastAppService appService;

	public VeracodeDastDashboardController(@Autowired ProductsService productsService,
			@Autowired VeracodeDastAppService appService) {
		this.productsService = productsService;
		this.appService = appService;
	}

	@GetMapping("")
	public SynapseModelAndView getDashboard() {
		SynapseModelAndView mav = new SynapseModelAndView("veracode-dast-dashboard");

		List<VeracodeDastAppModel> apps = appService.getAllApps();

		// Small Box stats
		mav.addObject("coveredApps", apps.size());
		mav.addObject("vulnApps",
				apps.size() > 0 ? apps.stream().filter(VeracodeDastAppModel::isVulnerable).count() : "N/A");
		LongSummaryStatistics scoreStats = apps.stream().filter(app -> app.getCurrentReport() != null)
				.collect(Collectors.summarizingLong(app -> app.getCurrentReport().getScore()));
		mav.addObject("scoreStats", scoreStats.getCount() > 0 ? scoreStats : null);
		mav.addObject("totalVulns",
				apps.size() > 0
						? apps.stream().filter(app -> app.getCurrentReport() != null)
								.mapToLong(app -> app.getCurrentReport().getUnmitigatedFlaws()).sum()
						: "N/A");

		// Product lines, project filters and projects for the graph dropdown menus
		mav.addObject("productLines", productsService.getAllProductLines());
		mav.addObject("filters", productsService.getAllProjectFilters());
		mav.addObject("projects", productsService.getAllProjects());

		mav.addStyleReference("/styles/veracodedast/veracode-dast-dashboard.css");
		mav.addScriptReference("/scripts/veracodedast/util.js");
		mav.addScriptReference("/scripts/veracodedast/veracode-dast-dashboard.js");
		return mav;
	}
}
