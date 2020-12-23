package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import com.tracelink.prodsec.plugin.jira.service.JiraScrumMetricsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
public class JiraScrumMetricsRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JiraScrumMetricsService mockScrumMetricsService;

	@Test
	@WithMockUser
	public void testGetAllMetricsByPeriod() throws Exception {
		String period = "last-week";
		JiraScrumMetric scrum = new JiraScrumMetric();
		scrum.setDone(1);
		scrum.setRecordedDate(LocalDate.now().minusDays(5));
		BDDMockito.when(mockScrumMetricsService.getAllScrumMetrics())
				.thenReturn(Arrays.asList(scrum));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/metrics/scrum").param("period", period))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Finished']", Matchers.hasItem(1)));
	}

	@Test
	@WithMockUser
	public void testGetAllMetricsByPeriodAllTime() throws Exception {
		String period = "all-time";

		JiraScrumMetric scrum1 = new JiraScrumMetric();
		scrum1.setRecordedDate(LocalDate.now());
		scrum1.setProg(1);
		JiraScrumMetric scrum2 = new JiraScrumMetric();
		scrum2.setRecordedDate(LocalDate.of(1970, 1, 1));
		scrum2.setDone(3);

		BDDMockito.when(mockScrumMetricsService.getOldestMetric()).thenReturn(scrum2);
		BDDMockito.when(mockScrumMetricsService.getAllScrumMetrics())
				.thenReturn(Arrays.asList(scrum1, scrum2));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/metrics/scrum").param("period", period))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['labels']", Matchers.hasItem("Jan 1970")));
	}

	@Test
	@WithMockUser
	public void testGetAllMetricsAllTimeNoFindings() throws Exception {
		String period = "all-time";
		String nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM"));
		if (LocalDate.now().getMonth().equals(Month.DECEMBER) || LocalDate.now().getMonth()
				.equals(Month.JANUARY)) {
			nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
		}

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/metrics/scrum").param("period", period))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['labels']", Matchers.contains(nowMonth)));
	}

	@Test
	@WithMockUser
	public void testGetAllMetricsBadPeriod() throws Exception {
		String period = "foobar";

		JiraScrumMetric metric = new JiraScrumMetric();
		BDDMockito.when(mockScrumMetricsService.getAllScrumMetrics())
				.thenReturn(Arrays.asList(metric));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/jira/rest/metrics/scrum").param("period", period))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['error']", Matchers.contains("Unknown time period.")));
	}
}
