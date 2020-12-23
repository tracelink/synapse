package com.tracelink.prodsec.plugin.jira.service;

import static org.mockito.Mockito.mock;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.tracelink.prodsec.plugin.jira.exception.JiraMappingsException;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.repo.JiraVulnMetricsRepo;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import io.atlassian.util.concurrent.Promise;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.joda.time.DateTime;
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

@RunWith(SpringRunner.class)
public class JiraVulnMetricsServiceTest {

	@MockBean
	private JiraVulnMetricsRepo mockVulnMetricsRepository;

	@MockBean
	private JiraClientConfigService mockClientConfigService;

	@MockBean
	private JiraAllowedSlaService mockAllowedSlaService;

	@MockBean
	private JiraPhrasesService mockJiraPhrasesService;

	@Mock
	private JiraRestClient mockRestClient;

	private JiraVulnMetricsService metricsService;

	@Before
	public void setup() {
		metricsService = new JiraVulnMetricsService(mockVulnMetricsRepository,
				mockClientConfigService, mockAllowedSlaService, mockJiraPhrasesService);
	}

	@Test
	public void testGetEarliesVulnMetricsDate() {
		//If the data base is empty, return null
		BDDMockito.when(mockVulnMetricsRepository.findTopByOrderByCreatedAsc()).thenReturn(null);
		Assert.assertNull(metricsService.getOldestMetrics());

		JiraVuln metric = new JiraVuln();
		metric.setCreated(LocalDate.now().minusDays(1));
		BDDMockito.when(mockVulnMetricsRepository.findTopByOrderByCreatedAsc()).thenReturn(metric);

		JiraVuln oldest = metricsService.getOldestMetrics();
		Assert.assertTrue(oldest.getCreated().isBefore(LocalDate.now()));

		Assert.assertEquals(oldest, metric);
	}

	@Test
	public void testStoreVulnMetrics()
			throws ExecutionException, InterruptedException, URISyntaxException {
		@SuppressWarnings("unchecked")
		Promise<SearchResult> promise = mock(Promise.class);
		SearchRestClient searchClient = mock(SearchRestClient.class);

		IssueField sevField = new IssueField("1", "Security Severity", null, "High");
		IssueField resolvedField = new IssueField("2", "Resolved", null,
				"1970-01-22T14:22:03.981-0500");
		ArrayList<IssueField> arrayIssueField = new ArrayList<>();
		arrayIssueField.add(sevField);
		arrayIssueField.add(resolvedField);

		Issue fakeIssue = new Issue(null, null, "ABC", (long) 1, null, null,
				null, null, null, null, null, null,
				null, DateTime.now(), null, null, null, null,
				null, null, arrayIssueField, null, null, null,
				null, null, null, null, null, null,
				null, null);

		Iterable<Issue> mockIssues = (new ArrayList<>(Collections.singleton(fakeIssue)));
		SearchResult result = new SearchResult(1, 1, 1, mockIssues);

		BDDMockito.when(mockClientConfigService.createRestClient()).thenReturn(mockRestClient);
		BDDMockito.when(mockRestClient.getSearchClient()).thenReturn(searchClient);
		BDDMockito.when(searchClient.searchJql(null, 100, 0, null)).thenReturn(promise);
		BDDMockito.when(promise.get()).thenReturn(result);

		try {
			metricsService.storeVulnMetrics();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		ArgumentCaptor<JiraVuln> captor = ArgumentCaptor.forClass(JiraVuln.class);
		BDDMockito.verify(mockVulnMetricsRepository, Mockito.times(1)).save(captor.capture());
		BDDMockito.verify(mockVulnMetricsRepository, Mockito.times(1)).flush();

	}

	@Test
	public void testCreateMapping() {
		JiraVuln vuln = new JiraVuln();
		ProductLineModel productLineModel = new ProductLineModel();

		BDDMockito.when(mockVulnMetricsRepository.findById((long) 2)).thenReturn(Optional.of(vuln));
		metricsService.createMapping(productLineModel, 2);

		ArgumentCaptor<JiraVuln> captor = ArgumentCaptor.forClass(JiraVuln.class);
		BDDMockito.verify(mockVulnMetricsRepository, Mockito.times(1))
				.saveAndFlush(captor.capture());
	}

	@Test(expected = JiraMappingsException.class)
	public void testCreateMappingFail() {
		ProductLineModel productLineModel = new ProductLineModel();
		BDDMockito.when(mockVulnMetricsRepository.findById((long) 2)).thenReturn(Optional.empty());
		metricsService.createMapping(productLineModel, 2);
	}

	@Test
	public void testDeleteMapping() {
		JiraVuln vuln = new JiraVuln();

		BDDMockito.when(mockVulnMetricsRepository.findById((long) 2)).thenReturn(Optional.of(vuln));
		metricsService.deleteMapping(2);

		ArgumentCaptor<JiraVuln> captor = ArgumentCaptor.forClass(JiraVuln.class);
		BDDMockito.verify(mockVulnMetricsRepository, Mockito.times(1))
				.saveAndFlush(captor.capture());
	}

	@Test(expected = JiraMappingsException.class)
	public void testDeleteMappingFail() {
		BDDMockito.when(mockVulnMetricsRepository.findById((long) 2)).thenReturn(Optional.empty());
		metricsService.deleteMapping(2);
	}

	@Test
	public void testGetAllVulnMetrics() {
		JiraVuln metric = new JiraVuln();
		BDDMockito.when(mockVulnMetricsRepository.findAll())
				.thenReturn(Collections.singletonList(metric));
		metricsService.getAllVulnMetrics();
		Assert.assertEquals(metric, metricsService.getAllVulnMetrics().get(0));
		Assert.assertEquals(1, metricsService.getAllVulnMetrics().size());

	}

	@Test
	public void testGetAllUnresolvedVulns() {
		JiraVuln unresolvedMetric = new JiraVuln();
		unresolvedMetric.setSev("High");
		unresolvedMetric.setCreated(LocalDate.now().minusDays(5));

		//When unresolved vuln is still in SLA
		BDDMockito.when(mockAllowedSlaService.getAllowedTimeBySev(unresolvedMetric.getSev()))
				.thenReturn(7);
		BDDMockito.when(mockVulnMetricsRepository.findAllByResolvedIsNull())
				.thenReturn(Collections.singletonList(unresolvedMetric));

		metricsService.getAllUnresolvedMetrics();

		Assert.assertEquals(unresolvedMetric, metricsService.getAllUnresolvedMetrics().get(0));
		Assert.assertEquals(metricsService.getAllUnresolvedMetrics().get(0).getSlaStatus(),
				"In SLA");
		Assert.assertEquals(1, metricsService.getAllUnresolvedMetrics().size());

		//When unresolved vuln is out of SLA
		BDDMockito.when(mockAllowedSlaService.getAllowedTimeBySev(unresolvedMetric.getSev()))
				.thenReturn(2);
		BDDMockito.when(mockVulnMetricsRepository.findAllByResolvedIsNull())
				.thenReturn(Collections.singletonList(unresolvedMetric));
		metricsService.getAllUnresolvedMetrics();
		Assert.assertEquals(metricsService.getAllUnresolvedMetrics().get(0).getSlaStatus(),
				"3 days past SLA");

		//When SLA doesn't matter for unresolved vuln
		BDDMockito.when(mockAllowedSlaService.getAllowedTimeBySev(unresolvedMetric.getSev()))
				.thenReturn(null);
		BDDMockito.when(mockVulnMetricsRepository.findAllByResolvedIsNull())
				.thenReturn(Collections.singletonList(unresolvedMetric));
		metricsService.getAllUnresolvedMetrics();
		Assert.assertEquals(metricsService.getAllUnresolvedMetrics().get(0).getSlaStatus(), "N/A");
	}

	@Test
	public void testGetAllUnresolvedVulnsforProductLine() {
		ProductLineModel plm = new ProductLineModel();

		JiraVuln unresolvedMetric = new JiraVuln();
		unresolvedMetric.setSev("High");
		unresolvedMetric.setCreated(LocalDate.now().minusDays(1));
		unresolvedMetric.setProductLine(plm);

		JiraVuln resolvedMetric = new JiraVuln();
		resolvedMetric.setSev("Critical");
		resolvedMetric.setCreated(LocalDate.now().minusDays(2));
		resolvedMetric.setResolved(LocalDate.now());

		List<JiraVuln> storedMetrics = new ArrayList<>();
		storedMetrics.add(unresolvedMetric);
		storedMetrics.add(resolvedMetric);

		BDDMockito.when(mockAllowedSlaService.getAllowedTimeBySev(resolvedMetric.getSev()))
				.thenReturn(2);
		BDDMockito.when(mockVulnMetricsRepository.findAllByProductLine(plm))
				.thenReturn(storedMetrics);

		metricsService.getUnresolvedVulnsForProductLine(plm);

		Assert.assertEquals(unresolvedMetric,
				metricsService.getUnresolvedVulnsForProductLine(plm).get(0));
		Assert.assertEquals(1, metricsService.getUnresolvedVulnsForProductLine(plm).size());
	}
}
