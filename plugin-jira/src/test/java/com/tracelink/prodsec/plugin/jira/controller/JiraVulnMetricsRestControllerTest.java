package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.mock.LoggerRule;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
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
public class JiraVulnMetricsRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JiraVulnMetricsService mockVulnMetricsService;

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(JiraVulnMetricsRestController.class);

	@Test
	@WithMockUser
	public void testGetAllVulnsByPeriod() throws Exception {
		//In bucket
		String period = "last-week";
		JiraVuln vuln1 = new JiraVuln();
		vuln1.setCreated(LocalDate.now().minusDays(5));
		vuln1.setSev("High");
		//Out of Bucket because out of date range
		JiraVuln vuln2 = new JiraVuln();
		vuln2.setCreated(LocalDate.now().minusDays(15));
		vuln2.setSev("Low");
		JiraVuln vuln3 = new JiraVuln();
		//Out of bucket because resolved
		vuln3.setCreated(LocalDate.now().minusDays(6));
		vuln3.setSev("Medium");
		vuln3.setResolved(LocalDate.now().minusDays(5));

		BDDMockito.when(mockVulnMetricsService.getAllVulnMetrics())
				.thenReturn(Arrays.asList(vuln1, vuln2, vuln3));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/vulnmetrics").param("period", period))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['High']", Matchers.hasItem(1)));
	}

	@Test
	@WithMockUser
	public void testGetAllVulnsByPeriodAllTime() throws Exception {
		String period = "all-time";

		JiraVuln vuln1 = new JiraVuln();
		vuln1.setCreated(LocalDate.now());
		vuln1.setSev("Unknown");
		JiraVuln vuln2 = new JiraVuln();
		vuln2.setCreated(LocalDate.of(1970, 1, 1));
		vuln2.setSev("Low");
		JiraVuln vuln3 = new JiraVuln();
		vuln3.setCreated(LocalDate.of(2000, 6, 1));
		vuln3.setSev("Critical");
		JiraVuln vuln4 = new JiraVuln();
		vuln4.setCreated(LocalDate.of(2010, 1, 1));
		vuln4.setSev("Medium");
		JiraVuln vuln5 = new JiraVuln();
		vuln5.setCreated(LocalDate.of(2020, 1, 1));
		vuln5.setSev("Informational");
		JiraVuln vuln6 = new JiraVuln();
		vuln6.setId(6);
		vuln6.setCreated(LocalDate.of(2020, 1, 1));
		vuln6.setSev("foobar");

		BDDMockito.when(mockVulnMetricsService.getOldestMetrics()).thenReturn(vuln2);
		BDDMockito.when(mockVulnMetricsService.getAllVulnMetrics())
				.thenReturn(Arrays.asList(vuln1, vuln2, vuln3, vuln4, vuln5, vuln6));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/vulnmetrics").param("period", period))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['labels']", Matchers.hasItem("Jan 1970")));
		Assert.assertEquals(
				"Unexpected value foobar returned as issue severity for issue " + vuln6.getId(),
				loggerRule.getMessages().get(0));
	}

	@Test
	@WithMockUser
	public void testGetAllVulnsAllTimeNoFindings() throws Exception {
		String period = "all-time";
		BDDMockito.when(mockVulnMetricsService.getAllVulnMetrics()).thenReturn(new ArrayList<>());
		String nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM"));
		if (LocalDate.now().getMonth().equals(Month.DECEMBER) || LocalDate.now().getMonth()
				.equals(Month.JANUARY)) {
			nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
		}

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/vulnmetrics").param("period", period))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['labels']", Matchers.contains(nowMonth)));
	}

	@Test
	@WithMockUser
	public void testGetAllVulnsBadPeriod() throws Exception {
		String period = "foobar";
		JiraVuln vuln = new JiraVuln();

		BDDMockito.when(mockVulnMetricsService.getAllVulnMetrics()).thenReturn(
				Collections.singletonList(vuln));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/vulnmetrics").param("period", period))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['error']", Matchers.contains("Unknown time period.")));
	}
}
