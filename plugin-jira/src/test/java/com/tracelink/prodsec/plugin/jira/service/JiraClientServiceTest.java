package com.tracelink.prodsec.plugin.jira.service;

import static org.mockito.Mockito.mock;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tracelink.prodsec.plugin.jira.exception.JiraClientException;
import com.tracelink.prodsec.plugin.jira.mock.LoggerRule;
import com.tracelink.prodsec.plugin.jira.model.JiraClient;
import com.tracelink.prodsec.plugin.jira.repo.JiraClientRepository;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class JiraClientServiceTest {

	@MockBean
	private JiraUpdateService updateService;

	@MockBean
	private JiraPhrasesService searchJqlService;

	@MockBean
	private JiraClientRepository mockClientRepository;

	private JiraClientConfigService clientService;

	private JiraClient client;

	@Rule
	public LoggerRule loggerRule = LoggerRule.forClass(JiraClientConfigService.class);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(
			WireMockConfiguration.wireMockConfig().dynamicPort());

	@Before
	public void setup() {
		this.clientService = new JiraClientConfigService(mockClientRepository);
		this.client = new JiraClient();
	}

	@Test
	public void testTestConnectionFailClient() {
		BDDMockito.when(mockClientRepository.findAll()).thenReturn(Collections.emptyList());
		Assert.assertFalse(updateService.testConnection());
	}

	@Test
	public void testTestConnectionFailJira() {
		BDDMockito.when(mockClientRepository.findAll())
				.thenReturn(Collections.singletonList(client));
		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.willReturn(WireMock.aResponse().withBody("invalid")));
		Assert.assertFalse(updateService.testConnection());
	}

	@Test
	public void testGetClientConfig() {
		BDDMockito.when(mockClientRepository.findAll()).thenReturn(Arrays.asList(client));
		Assert.assertEquals(client, clientService.getClient());
	}

	@Test(expected = JiraClientException.class)
	public void testGetClientConfigNull() {
		BDDMockito.when(mockClientRepository.findAll()).thenReturn(new ArrayList<>());
		Assert.assertNull(clientService.getClient());
	}

	@Test
	public void testGetApiClient() throws MalformedURLException {
		JiraClient config = new JiraClient();
		config.setApiUrl(new URL("http://foo.com"));
		config.setUser("1234");
		config.setAuth("5678");
		BDDMockito.when(mockClientRepository.findAll()).thenReturn(Arrays.asList(config));
		Assert.assertNotNull(clientService.getClient());
	}

	@Test
	public void testSetClientConfigNew() throws MalformedURLException {
		BDDMockito.when(mockClientRepository.findAll()).thenReturn(new ArrayList<>());
		URL apiUrl = new URL("http://foo.com");
		String user = "abc";
		String auth = "5678";
		BDDMockito.given(mockClientRepository.saveAndFlush(BDDMockito.any()))
				.willAnswer(e -> e.getArgument(0));
		JiraClient config = clientService.setClient(apiUrl, user, auth);
		Assert.assertEquals(apiUrl, config.getApiUrl());
		Assert.assertEquals(user, config.getUser());
		Assert.assertEquals(auth, config.getAuth());
	}

	@Test
	public void testSetClientConfigReplace() throws MalformedURLException {
		JiraClient config = new JiraClient();
		URL apiUrl = new URL("http://foo.com");
		String user = "abc";
		String auth = "1234";
		config.setApiUrl(apiUrl);
		config.setUser(user);
		config.setAuth(auth);

		URL newApiUrl = new URL("http://bar.com");
		String newUser = "def";
		String newAuth = "5678";
		BDDMockito.when(mockClientRepository.findAll()).thenReturn(Arrays.asList(config));
		BDDMockito.given(mockClientRepository.saveAndFlush(BDDMockito.any()))
				.willAnswer(e -> e.getArgument(0));

		JiraClient newConfig = clientService.setClient(newApiUrl, newUser, newAuth);
		Assert.assertEquals(newApiUrl, newConfig.getApiUrl());
		Assert.assertEquals(newUser, newConfig.getUser());
		Assert.assertEquals(newAuth, newConfig.getAuth());
	}

	@Test
	public void testCreateRestClient() throws URISyntaxException, MalformedURLException {
		URL url = new URL("https://foo.com");
		client.setApiUrl(url);
		client.setUser("user");
		client.setAuth("auth");
		BDDMockito.when(mockClientRepository.findAll())
				.thenReturn(Collections.singletonList(client));
		AsynchronousJiraRestClientFactory factory = mock(AsynchronousJiraRestClientFactory.class);
		BDDMockito.when(factory.createWithBasicHttpAuthentication(url.toURI(), "user", "auth"))
				.thenReturn(mock(JiraRestClient.class));

		clientService.createRestClient();
	}
}

