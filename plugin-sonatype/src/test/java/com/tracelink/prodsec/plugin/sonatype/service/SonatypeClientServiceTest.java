package com.tracelink.prodsec.plugin.sonatype.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tracelink.prodsec.plugin.sonatype.mock.LoggerRule;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeClient;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeClientRepository;
import com.tracelink.prodsec.plugin.sonatype.util.client.Application;
import com.tracelink.prodsec.plugin.sonatype.util.client.ApplicationTag;
import com.tracelink.prodsec.plugin.sonatype.util.client.ApplicationViolation;
import com.tracelink.prodsec.plugin.sonatype.util.client.ApplicationViolations;
import com.tracelink.prodsec.plugin.sonatype.util.client.Applications;
import com.tracelink.prodsec.plugin.sonatype.util.client.Component;
import com.tracelink.prodsec.plugin.sonatype.util.client.ComponentIdentifier;
import com.tracelink.prodsec.plugin.sonatype.util.client.ConstraintViolation;
import com.tracelink.prodsec.plugin.sonatype.util.client.Coordinates;
import com.tracelink.prodsec.plugin.sonatype.util.client.Policies;
import com.tracelink.prodsec.plugin.sonatype.util.client.Policy;
import com.tracelink.prodsec.plugin.sonatype.util.client.PolicyViolation;
import com.tracelink.prodsec.plugin.sonatype.util.client.Reason;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SonatypeClientServiceTest {

	@MockBean
	private SonatypeAppService appService;

	@MockBean
	private SonatypeMetricsService metricsService;

	@MockBean
	private SonatypeClientRepository clientRepository;

	private SonatypeClientService clientService;

	private SonatypeClient client;

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(SonatypeClientService.class);

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(
			WireMockConfiguration.wireMockConfig().dynamicPort());

	@Before
	public void setup() {
		clientService = new SonatypeClientService(appService, metricsService, clientRepository);
		client = new SonatypeClient();
		client.setApiUrl(wireMockRule.baseUrl());
		client.setUser("foo");
		client.setAuth("barbaz");
	}

	@Test
	public void testTestConnectionFailClient() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		Assert.assertFalse(clientService.testConnection());
		Assert.assertEquals("No Sonatype client configured.", loggerRule.getMessages().get(0));
	}

	@Test
	public void testTestConnectionFailSonatype() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.willReturn(WireMock.aResponse().withBody("invalid")));
		Assert.assertFalse(clientService.testConnection());
	}

	@Test
	public void testTestConnection() throws Exception {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		JSONObject json = new JSONObject().put("applications", new JSONArray());

		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.withBasicAuth(client.getUser(), client.getAuth())
				.willReturn(WireMock.okJson(json.toString())));
		Assert.assertTrue(clientService.testConnection());
	}

	@Test
	public void testFetchDataFailClient() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		clientService.fetchData();
		Assert.assertEquals("No Sonatype client configured.", loggerRule.getMessages().get(0));
	}

	@Test
	public void testFetchDataFailSonatypeApplications() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.willReturn(WireMock.aResponse().withBody("invalid")));
		clientService.fetchData();
		BDDMockito.verify(metricsService, Mockito.times(0))
				.storeMetrics(BDDMockito.any(SonatypeApp.class),
						BDDMockito.anyMap());
	}

	@Test
	public void testFetchDataFailSonatypePolicies() throws Exception {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		ApplicationTag appTag = new ApplicationTag();
		appTag.setId("jkl");
		appTag.setApplicationId("123");
		appTag.setTagId("Tag1");

		Application app = new Application();
		app.setId("123");
		app.setPublicId("App1");
		app.setName("App1");
		app.setOrganizationId("456");
		app.setContactUserName(null);
		app.setApplicationTags(Collections.singletonList(appTag));

		Applications apps = new Applications();
		apps.setApplications(Collections.singletonList(app));

		ObjectMapper om = new ObjectMapper();
		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.withBasicAuth(client.getUser(), client.getAuth())
				.willReturn(WireMock.okJson(om.writeValueAsString(apps))));

		WireMock.stubFor(WireMock.get("/api/v2/policies")
				.willReturn(WireMock.aResponse().withBody("invalid")));
		clientService.fetchData();
		BDDMockito.verify(metricsService, Mockito.times(0))
				.storeMetrics(BDDMockito.any(SonatypeApp.class),
						BDDMockito.anyMap());
	}

	@Test
	public void testFetchDataFailSonatypeApplicationViolations() throws Exception {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		ApplicationTag appTag = new ApplicationTag();
		appTag.setId("jkl");
		appTag.setApplicationId("123");
		appTag.setTagId("Tag1");

		Application app = new Application();
		app.setId("123");
		app.setPublicId("App1");
		app.setName("App1");
		app.setOrganizationId("456");
		app.setContactUserName(null);
		app.setApplicationTags(Collections.singletonList(appTag));

		Applications apps = new Applications();
		apps.setApplications(Collections.singletonList(app));

		Policy policy = new Policy();
		policy.setId("789");
		policy.setName("Policy1");
		policy.setOwnerId("ROOT");
		policy.setOwnerType("ORGANIZATION");
		policy.setThreatLevel(1);
		policy.setPolicyType("other");

		Policies policies = new Policies();
		policies.setPolicies(Collections.singletonList(policy));

		ObjectMapper om = new ObjectMapper();
		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.withBasicAuth(client.getUser(), client.getAuth())
				.willReturn(WireMock.okJson(om.writeValueAsString(apps))));
		WireMock.stubFor(
				WireMock.get("/api/v2/policies").withBasicAuth(client.getUser(), client.getAuth())
						.willReturn(WireMock.okJson(om.writeValueAsString(policies))));

		WireMock.stubFor(
				WireMock.get("/api/v2/policyViolations?p=789")
						.willReturn(WireMock.aResponse().withBody("invalid")));
		clientService.fetchData();
		BDDMockito.verify(metricsService, Mockito.times(0))
				.storeMetrics(BDDMockito.any(SonatypeApp.class),
						BDDMockito.anyMap());
	}

	@Test
	public void testFetchData() throws Exception {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		ApplicationTag appTag = new ApplicationTag();
		appTag.setId("jkl");
		appTag.setApplicationId("123");
		appTag.setTagId("Tag1");

		Application app = new Application();
		app.setId("123");
		app.setPublicId("App1");
		app.setName("App1");
		app.setOrganizationId("456");
		app.setContactUserName(null);
		app.setApplicationTags(Collections.singletonList(appTag));

		Applications apps = new Applications();
		apps.setApplications(Collections.singletonList(app));

		Policy policy = new Policy();
		policy.setId("789");
		policy.setName("Policy1");
		policy.setOwnerId("ROOT");
		policy.setOwnerType("ORGANIZATION");
		policy.setThreatLevel(1);
		policy.setPolicyType("other");

		Policies policies = new Policies();
		policies.setPolicies(Collections.singletonList(policy));

		Reason reason = new Reason();
		reason.setReason("Found security vulnerability.");

		ConstraintViolation cv = new ConstraintViolation();
		cv.setConstraintId("def");
		cv.setConstraintName("High risk");
		cv.setReasons(Collections.singletonList(reason));

		Coordinates coordinates = new Coordinates();
		coordinates.setArtifactId("artifact");
		coordinates.setClassifier("");
		coordinates.setExtension("jar");
		coordinates.setGroupId("com.example");
		coordinates.setVersion("1.0.0");
		coordinates.setName("coordinates");
		coordinates.setQualifier("qualifier");

		ComponentIdentifier ci = new ComponentIdentifier();
		ci.setFormat("maven");
		ci.setCoordinates(coordinates);

		Component component = new Component();
		component.setPackageUrl("pkg");
		component.setHash("ghi");
		component.setComponentIdentifier(ci);
		component.setProprietary(false);

		PolicyViolation policyViolation = new PolicyViolation();
		policyViolation.setPolicyId("789");
		policyViolation.setPolicyName("Security-High");
		policyViolation.setPolicyViolationId("abc");
		policyViolation.setThreatLevel(9);
		policyViolation.setConstraintViolations(Collections.singletonList(cv));
		policyViolation.setStageId("build");
		policyViolation.setReportUrl("report/url");
		policyViolation.setComponent(component);

		ApplicationViolation appVio = new ApplicationViolation();
		appVio.setApplication(app);
		appVio.setPolicyViolations(Collections.singletonList(policyViolation));

		ApplicationViolations appVios = new ApplicationViolations();
		appVios.setApplicationViolations(Collections.singletonList(appVio));

		ObjectMapper om = new ObjectMapper();
		WireMock.stubFor(WireMock.get("/api/v2/applications")
				.withBasicAuth(client.getUser(), client.getAuth())
				.willReturn(WireMock.okJson(om.writeValueAsString(apps))));
		WireMock.stubFor(
				WireMock.get("/api/v2/policies").withBasicAuth(client.getUser(), client.getAuth())
						.willReturn(WireMock.okJson(om.writeValueAsString(policies))));
		WireMock.stubFor(
				WireMock.get("/api/v2/policyViolations?p=789")
						.withBasicAuth(client.getUser(), client.getAuth())
						.willReturn(WireMock.okJson(om.writeValueAsString(appVios))));

		SonatypeApp sonatypeApp = new SonatypeApp();
		sonatypeApp.setId("123");
		sonatypeApp.setName("App1");
		BDDMockito.when(appService.getAppForId("123", "App1")).thenReturn(sonatypeApp);

		clientService.fetchData();
		BDDMockito.verify(metricsService, Mockito.times(1))
				.storeMetrics(BDDMockito.any(SonatypeApp.class),
						BDDMockito.anyMap());
	}

	@Test
	public void testSetClientNull() {
		Assert.assertFalse(clientService.setClient(null, "foo", "bar"));
	}

	@Test
	public void testSetClientInvalidUrl() {
		Assert.assertFalse(clientService.setClient("foo", "bar", "baz"));
	}

	@Test
	public void testSetClient() {
		// Case where no client is set
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		Assert.assertTrue(clientService.setClient("http://example.com", "user", "auth"));
		BDDMockito.verify(clientRepository, Mockito.times(1))
				.saveAndFlush(BDDMockito.any(SonatypeClient.class));

		// Case where no client is set
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		Assert.assertTrue(clientService.setClient("http://example.com", "user", "auth"));
		BDDMockito.verify(clientRepository, Mockito.times(1)).saveAndFlush(client);
		Assert.assertEquals("http://example.com", client.getApiUrl());
		Assert.assertEquals("user", client.getUser());
		Assert.assertEquals("auth", client.getAuth());
	}
}
