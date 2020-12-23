package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * The Controller is used to provide the additional URL path, /vulns to respond on.
 * The /vuln page displays gathered vulnerability metrics as a graph displaying
 * vulnerability presence over time, and a table with the unresolved vulnerablities
 * listed.
 *
 * @author bhoran
 */
@Controller
@RequestMapping(JiraPlugin.VULN_PAGE)
public class JiraVulnMetricsController {

	private final JiraVulnMetricsService vulnMetricsService;

	public JiraVulnMetricsController(@Autowired JiraVulnMetricsService vulnMetricsService) {
		this.vulnMetricsService = vulnMetricsService;
	}

	@GetMapping("")
	public SynapseModelAndView jiraMetrics() {
		SynapseModelAndView smav = new SynapseModelAndView("jira/vulns");

		List<JiraVuln> unresolvedVulns = vulnMetricsService.getAllUnresolvedMetrics();

		if (unresolvedVulns != null) {
			smav.addObject("unresolvedVulns", unresolvedVulns);
		}

		smav.addScriptReference("/scripts/jira/vulnerabilities/vuln-bar.js");
		smav.addScriptReference("/scripts/jira/vulnerabilities/datatable.js");
		smav.addScriptReference("/scripts/jira/utils.js");

		return smav;
	}
}
