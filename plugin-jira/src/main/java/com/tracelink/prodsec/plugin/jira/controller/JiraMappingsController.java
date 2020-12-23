package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.exception.JiraMappingsException;
import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * The Controller is used to provide the additional URL path, /mappings, to respond
 * on, allowing the user to associate a vulnerability with a product line by
 * creating a mapping between the two. The JiraMappingsController allows the user
 * to create and delete these connections, which are both on the mappings page and
 * when examining individual unresolved vulnerabilities on the /vuln page
 *
 * @author bhoran
 */
@Controller
@RequestMapping(JiraPlugin.MAPPINGS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class JiraMappingsController {

	private static final String MAPPINGS_REDIRECT = "redirect:" + JiraPlugin.MAPPINGS_PAGE;

	private final JiraVulnMetricsService jiraVulnService;
	private final ProductsService productsService;


	public JiraMappingsController(@Autowired JiraVulnMetricsService jiraVulnService,
			@Autowired ProductsService productsService) {
		this.jiraVulnService = jiraVulnService;
		this.productsService = productsService;
	}

	@GetMapping("")
	public SynapseModelAndView jiraMappings() {
		SynapseModelAndView mv = new SynapseModelAndView("jira/mappings");

		List<ProductLineModel> synapseProducts = productsService.getAllProductLines();

		List<JiraVuln> vulnerabilities = jiraVulnService.getAllVulnMetrics();
		List<JiraVuln> unmappedVulns = new ArrayList<>();
		List<JiraVuln> mappedVulns = new ArrayList<>();

		for (JiraVuln v : vulnerabilities) {
			if (v.getProductLine() == null) {
				unmappedVulns.add(v);
			} else {
				mappedVulns.add(v);
			}
		}

		mv.addObject("synapseProducts", synapseProducts);
		mv.addObject("vulnerabilities", vulnerabilities);
		mv.addObject("mappedVulns", mappedVulns);
		mv.addObject("unmappedVulns", unmappedVulns);
		return mv;
	}

	@PostMapping("create")
	public String createMapping(@RequestParam String synapseProduct,
			@RequestParam Long vulnId, RedirectAttributes redirectAttributes) {
		if (StringUtils.isEmpty(synapseProduct)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide all inputs.");
			return MAPPINGS_REDIRECT;
		}

		try {
			jiraVulnService.createMapping(productsService.getProductLine(synapseProduct), vulnId);
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping.");
		} catch (JiraMappingsException e) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return MAPPINGS_REDIRECT;
	}

	@PostMapping("delete")
	public String deleteMapping(@RequestParam Long vulnId, RedirectAttributes redirectAttributes) {
		try {
			jiraVulnService.deleteMapping(vulnId);
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Deleted mapping.");
		} catch (JiraMappingsException e) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return MAPPINGS_REDIRECT;
	}
}
