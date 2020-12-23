package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import com.tracelink.prodsec.plugin.jira.service.JiraScrumMetricsService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Controller is used to provide the additional URL path, /scrum to respond on.
 * The /scrum page displays gathered scrum metrics and displays the information
 * in the form of indivudual statistics and a graph displaying scrum changes over time.
 *
 * @author bhoran
 */
@Controller
@RequestMapping(JiraPlugin.SCRUM_PAGE)
public class JiraScrumMetricsController {

	private final JiraScrumMetricsService scrumMetricsService;

	public JiraScrumMetricsController(@Autowired JiraScrumMetricsService scrumMetricsService) {
		this.scrumMetricsService = scrumMetricsService;
	}

	@GetMapping("")
	public SynapseModelAndView jiraScrum() {
		SynapseModelAndView smav = new SynapseModelAndView("jira/scrum");

		JiraScrumMetric scrumMetricEntity = scrumMetricsService.getMostRecent();

		if (scrumMetricEntity != null) {
			long unres = scrumMetricEntity.getUnres();
			long total = scrumMetricEntity.getTotal();
			smav.addObject("unres", unres > 0 ? unres : 0);
			smav.addObject("total", total > 0 ? total : 0);
			smav.addObject("scrumMetrics", scrumMetricEntity);
		}
		smav.addScriptReference("/scripts/jira/scrum/scrum-bar.js");
		smav.addScriptReference("/scripts/jira/utils.js");
		return smav;
	}
}
