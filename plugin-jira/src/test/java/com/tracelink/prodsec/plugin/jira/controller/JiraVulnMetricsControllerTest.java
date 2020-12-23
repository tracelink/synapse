package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class JiraVulnMetricsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JiraVulnMetricsService mockVulnMetricService;

	@Test
	@WithMockUser()
	public void testGetVulnMetricsNoInfo() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.VULN_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("jira/vulns")))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.contains(
						"/scripts/jira/vulnerabilities/vuln-bar.js",
						"/scripts/jira/vulnerabilities/datatable.js",
						"/scripts/jira/utils.js")));
	}

	@Test
	@WithMockUser()
	public void testGetVulnMetrics() throws Exception {
		JiraVuln unresolvedMetric = new JiraVuln();
		BDDMockito.when(mockVulnMetricService.getAllUnresolvedMetrics())
				.thenReturn(Arrays.asList(unresolvedMetric));
		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.VULN_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("jira/vulns")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("unresolvedVulns", Matchers.hasItems(unresolvedMetric)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.contains(
						"/scripts/jira/vulnerabilities/vuln-bar.js",
						"/scripts/jira/vulnerabilities/datatable.js",
						"/scripts/jira/utils.js")));
	}
}
