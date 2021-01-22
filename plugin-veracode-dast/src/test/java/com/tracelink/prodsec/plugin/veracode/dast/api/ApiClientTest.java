package com.tracelink.prodsec.plugin.veracode.dast.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.applist.AppType;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.applist.Applist;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.buildlist.BuildType;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.detailedreport.CategoryType;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.detailedreport.CweType;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.detailedreport.FlawListType;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.detailedreport.FlawType;
import com.tracelink.prodsec.plugin.veracode.dast.api.data.detailedreport.SeverityType;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastClientConfigModel;
import com.veracode.apiwrapper.AbstractAPIWrapper;
import com.veracode.http.WebClient;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApiClientTest {

	private static final String GETAPPLIST = "/api/5.0/getapplist.do";

	private static final String GETSBXLIST = "/api/5.0/getsandboxlist.do";

	private static final String GETBUILDLIST = "/api/5.0/getbuildlist.do";

	private static final String DETAILEDREPORT = "/api/5.0/detailedreport.do";

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(
			WireMockConfiguration.wireMockConfig().dynamicPort());

	private void injectWireMock(ApiClient client)
			throws IllegalArgumentException, IllegalAccessException {
		List<String> wrapperFieldNames = Arrays
				.asList("uploadWrapper", "sandboxWrapper", "resultsWrapper");
		for (String fieldName : wrapperFieldNames) {
			WebClient webClient = getWrappedClient(client, fieldName);
			webClient.baseAddress = wireMockRule.baseUrl();
		}
	}

	private WebClient getWrappedClient(ApiClient client, String wrapperName)
			throws IllegalArgumentException, IllegalAccessException {
		Field wrapperField = ReflectionUtils.findField(ApiClient.class, wrapperName);
		wrapperField.setAccessible(true);
		AbstractAPIWrapper wrapper = (AbstractAPIWrapper) wrapperField.get(client);
		Field webClientField = ReflectionUtils.findField(AbstractAPIWrapper.class, "webClient");
		webClientField.setAccessible(true);
		return (WebClient) webClientField.get(wrapper);
	}

	private ApiClient makeDefaultTestClient()
			throws IllegalArgumentException, IllegalAccessException {
		ApiClient client = new ApiClient();
		injectWireMock(client);
		return client;
	}

	private <T> String toXML(T o, Class<T> target) throws Exception {
		JAXBContext jaxb = JAXBContext.newInstance(target);
		Marshaller marshaller = jaxb.createMarshaller();
		StringWriter sw = new StringWriter();
		marshaller.marshal(o, sw);
		return sw.toString();
	}

	@Test
	public void testSetupConfig() throws Exception {
		ApiClient client = makeDefaultTestClient();
		String fakeApiKey = "0000";
		String fakeApiId = "FFFF";
		VeracodeDastClientConfigModel config = new VeracodeDastClientConfigModel();
		config.setApiId(fakeApiId);
		config.setApiKey(fakeApiKey);
		WebClient webClient = getWrappedClient(client, "uploadWrapper");
		client.setConfig(config);
		WireMock.stubFor(WireMock.post("/test").willReturn(WireMock.aResponse().withStatus(200)));
		webClient.downloadString("/test");
		WireMock.verify(
				WireMock.postRequestedFor(WireMock.urlEqualTo("/test")).withHeader("Authorization",
						WireMock.containing("VERACODE-HMAC")));
	}

	@Test
	public void testTestAccess() throws Exception {
		ApiClient client = makeDefaultTestClient();

		try {
			client.testAccess();
			Assert.fail("Should have failed");
		} catch (VeracodeClientException e) {
			// correct
		}
		Applist apps = new Applist();

		String appListEmptyXml = toXML(apps, Applist.class);
		WireMock.stubFor(
				WireMock.post(GETAPPLIST).willReturn(
						WireMock.aResponse().withStatus(200).withBody(appListEmptyXml)));

		try {
			client.testAccess();
			Assert.fail("Should have failed");
		} catch (VeracodeClientException e) {
			// correct
		}

		long appId = 123;
		AppType app = new AppType();
		app.setAppId(appId);
		apps.getApp().add(app);

		String appListXml = toXML(apps, Applist.class);
		WireMock.stubFor(
				WireMock.post(GETAPPLIST)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(appListXml)));

		try {
			client.testAccess();
			Assert.fail("Should have failed");
		} catch (VeracodeClientException e) {
			// correct
		}

		long buildId = 789;
		Buildlist builds = new Buildlist();
		BuildType build = new BuildType();
		build.setBuildId(buildId);
		builds.getBuild().add(build);

		String buildListXml = toXML(builds, Buildlist.class);
		WireMock.stubFor(
				WireMock.post(GETBUILDLIST)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(buildListXml)));

		try {
			client.testAccess();
			Assert.fail("Should have failed");
		} catch (VeracodeClientException e) {
			// correct
		}

		Detailedreport report = new Detailedreport();
		String reportXml = toXML(report, Detailedreport.class);
		WireMock.stubFor(
				WireMock.post(DETAILEDREPORT)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(reportXml)));

		client.testAccess();

		// needed to avoid extra unmatched request issues
		WireMock.resetAllRequests();
	}

	@Test
	public void testGetDetailedReport() throws Exception {
		ApiClient client = makeDefaultTestClient();

		long analysisId = 157;
		Detailedreport report = new Detailedreport();
		report.setAnalysisId(analysisId);
		String reportXml = toXML(report, Detailedreport.class);
		WireMock.stubFor(
				WireMock.post(DETAILEDREPORT)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(reportXml)));

		Detailedreport returnedReport = client.getDetailedReport("");
		Assert.assertEquals(analysisId, returnedReport.getAnalysisId());
	}

	@Test
	public void testGetDetailedReportTooManyFlaws() throws Exception {
		ApiClient client = makeDefaultTestClient();

		long analysisId = 157;
		Detailedreport report = new Detailedreport();
		report.setAnalysisId(analysisId);
		// Add 501 flaws to the report
		List<FlawType> flaws = IntStream.range(0, 501).mapToObj(i -> {
			FlawType flaw = new FlawType();
			flaw.setCount(1);
			return flaw;
		}).collect(Collectors.toList());

		FlawListType flawList = new FlawListType();
		flawList.setFlaw(flaws);
		CweType cwe = new CweType();
		cwe.setDynamicflaws(flawList);
		CategoryType category = new CategoryType();
		category.setCwe(Collections.singletonList(cwe));
		SeverityType severity = new SeverityType();
		severity.setCategory(Collections.singletonList(category));
		report.setSeverity(Collections.singletonList(severity));

		String reportXml = toXML(report, Detailedreport.class);
		WireMock.stubFor(
				WireMock.post(DETAILEDREPORT)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(reportXml)));

		try {
			client.getDetailedReport("12345678");
			Assert.fail("Should have thrown exception");
		} catch (VeracodeClientException e) {
			Assert.assertEquals(
					"Cannot parse detailed report for buildid 12345678 because it contains 501 flaws",
					e.getMessage());
		}
	}

	@Test
	public void testGetApplications() throws Exception {
		ApiClient client = makeDefaultTestClient();

		long appid = 123;
		String appName = "test";

		AppType app = new AppType();
		app.setAppId(appid);
		app.setAppName(appName);

		Applist apps = new Applist();
		apps.getApp().add(app);

		String xmlString = toXML(apps, Applist.class);

		WireMock.stubFor(
				WireMock.post(GETAPPLIST)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(xmlString)));

		Applist returnedApps = client.getApplications();
		Assert.assertEquals(1, returnedApps.getApp().size());

		AppType returnedApp = returnedApps.getApp().get(0);
		Assert.assertEquals(Long.valueOf(appid), returnedApp.getAppId());
		Assert.assertEquals(appName, returnedApp.getAppName());
	}

	@Test
	public void testGetBuildList() throws Exception {
		ApiClient client = makeDefaultTestClient();

		long buildId = 789;
		Buildlist builds = new Buildlist();
		BuildType build = new BuildType();
		build.setBuildId(buildId);
		builds.getBuild().add(build);

		String buildListXml = toXML(builds, Buildlist.class);
		WireMock.stubFor(
				WireMock.post(GETBUILDLIST)
						.willReturn(WireMock.aResponse().withStatus(200).withBody(buildListXml)));

		Buildlist returnedBuilds = client.getBuildList("");
		Assert.assertEquals(1, returnedBuilds.getBuild().size());

		BuildType returnedBuild = returnedBuilds.getBuild().get(0);
		Assert.assertEquals(Long.valueOf(buildId), returnedBuild.getBuildId());
	}

}
