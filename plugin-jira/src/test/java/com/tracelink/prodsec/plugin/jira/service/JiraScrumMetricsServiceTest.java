package com.tracelink.prodsec.plugin.jira.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import com.tracelink.prodsec.plugin.jira.repo.JiraScrumMetricsRepo;
import io.atlassian.util.concurrent.Promise;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class JiraScrumMetricsServiceTest {

	@MockBean
	private JiraScrumMetricsRepo mockScrumMetricsRepository;

	@MockBean
	private JiraClientConfigService mockClientConfigService;

	@MockBean
	private JiraPhrasesService mockJiraPhrasesService;

	@Mock
	private JiraRestClient mockRestClient;

	private JiraScrumMetricsService metricsService;

	@Before
	public void setup() {
		metricsService = new JiraScrumMetricsService(mockScrumMetricsRepository,
				mockClientConfigService, mockJiraPhrasesService);
	}

	@Test
	public void testGetEarliestMetricsDate() {
		BDDMockito.when(mockScrumMetricsRepository.findTopByOrderByRecordedDateAsc())
				.thenReturn(null);
		Assert.assertNull(metricsService.getOldestMetric());

		JiraScrumMetric metric = new JiraScrumMetric();
		metric.setRecordedDate(LocalDate.now().minusDays(1));
		BDDMockito.when(mockScrumMetricsRepository.findTopByOrderByRecordedDateAsc())
				.thenReturn(metric);

		JiraScrumMetric oldest = metricsService.getOldestMetric();
		Assert.assertTrue(oldest.getRecordedDate().isBefore(LocalDate.now()));

		Assert.assertEquals(oldest, metric);
	}

	@Test
	public void testStoreMetrics() throws Exception {
		SearchRestClient searchClient = mock(SearchRestClient.class);
		@SuppressWarnings("unchecked")
		Promise<SearchResult> promise = mock(Promise.class);
		SearchResult result = mock(SearchResult.class);

		BDDMockito.when(mockJiraPhrasesService.getPhraseForData(JiraPhraseDataFormat.SCRUM)).thenReturn("Scrum");
		BDDMockito.when(mockClientConfigService.createRestClient()).thenReturn(mockRestClient);
		BDDMockito.when(mockRestClient.getSearchClient()).thenReturn(searchClient);
		BDDMockito.when(searchClient.searchJql("Scrum")).thenReturn(promise);
		BDDMockito.when(promise.get()).thenReturn(result);
		BDDMockito.when(result.getTotal()).thenReturn(5);

		try {
			metricsService.storeScrumMetrics();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		ArgumentCaptor<JiraScrumMetric> captor = ArgumentCaptor.forClass(JiraScrumMetric.class);
		BDDMockito.verify(mockScrumMetricsRepository, Mockito.times(1))
				.saveAndFlush(captor.capture());

		JiraScrumMetric metrics = captor.getValue();
		Assert.assertEquals(5, metrics.getTodo());
		Assert.assertEquals(5, metrics.getProg());
		Assert.assertEquals(5, metrics.getBlock());
		Assert.assertEquals(5, metrics.getDone());
	}

	@Test
	public void testGetAllScrumMetrics() {
		JiraScrumMetric metric = new JiraScrumMetric();
		BDDMockito.when(mockScrumMetricsRepository.findAll())
				.thenReturn(Collections.singletonList(metric));
		metricsService.getAllScrumMetrics();
		Assert.assertEquals(metric, metricsService.getAllScrumMetrics().get(0));
		Assert.assertEquals(1, metricsService.getAllScrumMetrics().size());

	}

	@Test
	public void testGetMostRecent() {
		BDDMockito.when(mockScrumMetricsRepository.findTopByOrderByRecordedDateDesc())
				.thenReturn(null);
		Assert.assertNull(metricsService.getMostRecent());

		JiraScrumMetric metric = new JiraScrumMetric();
		metric.setRecordedDate(LocalDate.now().minusDays(1));

		BDDMockito.when(mockScrumMetricsRepository.findTopByOrderByRecordedDateDesc())
				.thenReturn(metric);

		JiraScrumMetric recent = metricsService.getMostRecent();

		Assert.assertEquals(recent, metric);
	}
}
