package com.tracelink.prodsec.plugin.jira.service;

import static org.mockito.Mockito.mock;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.tracelink.prodsec.plugin.jira.mock.LoggerRule;
import com.tracelink.prodsec.plugin.jira.model.JiraClient;
import io.atlassian.util.concurrent.Promise;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class JiraUpdateServiceTest {

	@MockBean
	private JiraVulnMetricsService mockVulnMetricsService;

	@MockBean
	private JiraScrumMetricsService mockScrumMetricsService;

	@MockBean
	private JiraClientConfigService mockConfigService;

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(JiraUpdateService.class);

	private JiraUpdateService updateService;

	@Before
	public void setup() {
		this.updateService = new JiraUpdateService(mockScrumMetricsService, mockVulnMetricsService,
				mockConfigService);
	}

	@Test
	public void testTestConnection()
			throws URISyntaxException, ExecutionException, InterruptedException {
		JiraRestClient mockRestClient = mock(JiraRestClient.class);
		SearchRestClient searchClient = mock(SearchRestClient.class);
		@SuppressWarnings("unchecked")
		Promise<SearchResult> promise = mock(Promise.class);
		SearchResult result = mock(SearchResult.class);

		BDDMockito.when(mockConfigService.createRestClient()).thenReturn(mockRestClient);
		BDDMockito.when(mockRestClient.getSearchClient()).thenReturn(searchClient);
		BDDMockito.when(searchClient.searchJql("type = Bug")).thenReturn(promise);
		BDDMockito.when(promise.get()).thenReturn(result);
		BDDMockito.when(result.getTotal()).thenReturn(5);

		Assert.assertTrue(updateService.testConnection());
	}

	@Test
	public void testSyncAllDataSuccess() {
		BDDMockito.when(mockConfigService.getClient()).thenReturn(new JiraClient());
		updateService.syncAllData();

		Assert.assertEquals("Beginning Jira data update", loggerRule.getMessages().get(0));
		Assert.assertEquals("Jira data update complete", loggerRule.getMessages().get(1));
	}

	@Test
	public void testSyncAllDataNoClient() {
		BDDMockito.when(mockConfigService.getClient()).thenReturn(null);
		updateService.syncAllData();
		Assert.assertEquals("Beginning Jira data update", loggerRule.getMessages().get(0));
		Assert.assertEquals("No Configuration for Jira client", loggerRule.getMessages().get(1));
	}
}
