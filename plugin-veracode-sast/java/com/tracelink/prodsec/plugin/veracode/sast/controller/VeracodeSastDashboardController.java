package com.tracelink.prodsec.plugin.veracode.sast.controller;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Dashboard controller shows the primary dashboard of statistics for this
 * plugin
 *
 * @author csmith
 */
@Controller
@RequestMapping(VeracodeSastPlugin.DASHBOARD_PAGE)
public class VeracodeSastDashboardController {

	private final ProductsService productsService;
	private final VeracodeSastAppService appService;

	public VeracodeSastDashboardController(@Autowired ProductsService productsService,
			@Autowired VeracodeSastAppService appService) {
		this.productsService = productsService;
		this.appService = appService;
	}

	@GetMapping("")
	public SynapseModelAndView getDashboard() {
		SynapseModelAndView mav = new SynapseModelAndView("veracode-sast-dashboard");

		List<VeracodeSastAppModel> apps = appService.getIncludedApps();

		// Small Box stats
		mav.addObject("coveredApps", apps.size());
		mav.addObject("vulnApps",
				apps.size() > 0 ? apps.stream().filter(VeracodeSastAppModel::isVulnerable).count()
						: "N/A");
		LongSummaryStatistics scoreStats = apps.stream()
				.filter(app -> app.getCurrentReport() != null)
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

		mav.addStyleReference("/styles/veracodesast/veracode-sast-dashboard.css");
		mav.addScriptReference("/scripts/veracodesast/util.js");
		mav.addScriptReference("/scripts/veracodesast/veracode-sast-dashboard.js");
		return mav;
	}
}
