package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import com.tracelink.prodsec.plugin.jira.service.JiraScrumMetricsService;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class JiraScrumMetricsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JiraScrumMetricsService mockScrumMetricService;

	@Test
	@WithMockUser()
	public void testGetScrumMetricsNoInfo() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.SCRUM_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("jira/scrum")))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.contains(
						"/scripts/jira/scrum/scrum-bar.js", "/scripts/jira/utils.js")));
	}

	@Test
	@WithMockUser()
	public void testGetScrumMetrics() throws Exception {
		JiraScrumMetric recentMetric = new JiraScrumMetric();
		BDDMockito.when(mockScrumMetricService.getMostRecent()).thenReturn(recentMetric);

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.SCRUM_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("jira/scrum")))
				.andExpect(MockMvcResultMatchers.model().attribute("unres", Matchers.is(0L)))
				.andExpect(MockMvcResultMatchers.model().attribute("total", Matchers.is(0L)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("scrumMetrics", Matchers.is(recentMetric)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.contains(
						"/scripts/jira/scrum/scrum-bar.js", "/scripts/jira/utils.js")));
	}
}
