package com.tracelink.prodsec.lib.veracode.xml.api;

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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiClient;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.api.xml.data.applist.AppType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.applist.Applist;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.CategoryType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.CweType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.FlawListType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.FlawType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.SeverityType;
import com.veracode.apiwrapper.AbstractAPIWrapper;
import com.veracode.http.WebClient;

public class ApiClientTest {

	private static final String GETAPPLIST = "/api/5.0/getapplist.do";

	private static final String GETBUILDLIST = "/api/5.0/getbuildlist.do";

	private static final String DETAILEDREPORT = "/api/5.0/detailedreport.do";

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

	private void injectWireMock(VeracodeXmlApiClient client) throws Exception {
		List<String> wrapperFieldNames = Arrays.asList("uploadWrapper", "sandboxWrapper", "resultsWrapper");
		for (String fieldName : wrapperFieldNames) {
			WebClient webClient = getWrappedClient(client, fieldName);
			webClient.baseAddress = wireMockRule.baseUrl();
		}
	}

	private WebClient getWrappedClient(VeracodeXmlApiClient client, String wrapperName) throws Exception {
		Field wrapperField = VeracodeXmlApiClient.class.getDeclaredField(wrapperName);
		wrapperField.setAccessible(true);
		AbstractAPIWrapper wrapper = (AbstractAPIWrapper) wrapperField.get(client);
		Field webClientField = AbstractAPIWrapper.class.getDeclaredField("webClient");
		webClientField.setAccessible(true);
		return (WebClient) webClientField.get(wrapper);
	}

	private VeracodeXmlApiClient makeDefaultTestClient() throws Exception {
		return makeDefaultTestClient("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
	}

	private VeracodeXmlApiClient makeDefaultTestClient(String apiId, String apiKey) throws Exception {
		VeracodeXmlApiClient client = new VeracodeXmlApiClient(apiId, apiKey);
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
		VeracodeXmlApiClient client = makeDefaultTestClient();

		WebClient webClient = getWrappedClient(client, "uploadWrapper");
		WireMock.stubFor(WireMock.post("/test").willReturn(WireMock.aResponse().withStatus(200)));
		webClient.downloadString("/test");
		WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/test")).withHeader("Authorization",
				WireMock.containing("VERACODE-HMAC")));
	}


	@Test
	public void testGetDetailedReport() throws Exception {
		VeracodeXmlApiClient client = makeDefaultTestClient();

		long analysisId = 157;
		Detailedreport report = new Detailedreport();
		report.setAnalysisId(analysisId);
		String reportXml = toXML(report, Detailedreport.class);
		WireMock.stubFor(
				WireMock.post(DETAILEDREPORT).willReturn(WireMock.aResponse().withStatus(200).withBody(reportXml)));

		Detailedreport returnedReport = client.getDetailedReport("");
		Assert.assertEquals(analysisId, returnedReport.getAnalysisId());
	}

	@Test
	public void testGetDetailedReportTooManyFlaws() throws Exception {
		VeracodeXmlApiClient client = makeDefaultTestClient();

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
				WireMock.post(DETAILEDREPORT).willReturn(WireMock.aResponse().withStatus(200).withBody(reportXml)));

		try {
			client.getDetailedReport("12345678");
			Assert.fail("Should have thrown exception");
		} catch (VeracodeXmlApiException e) {
			Assert.assertEquals("Cannot parse detailed report for buildid 12345678 because it contains 501 flaws",
					e.getMessage());
		}
	}

	@Test
	public void testGetApplications() throws Exception {
		VeracodeXmlApiClient client = makeDefaultTestClient();

		long appid = 123;
		String appName = "test";

		AppType app = new AppType();
		app.setAppId(appid);
		app.setAppName(appName);

		Applist apps = new Applist();
		apps.getApp().add(app);

		String xmlString = toXML(apps, Applist.class);

		WireMock.stubFor(
				WireMock.post(GETAPPLIST).willReturn(WireMock.aResponse().withStatus(200).withBody(xmlString)));

		Applist returnedApps = client.getApplications();
		Assert.assertEquals(1, returnedApps.getApp().size());

		AppType returnedApp = returnedApps.getApp().get(0);
		Assert.assertEquals(Long.valueOf(appid), returnedApp.getAppId());
		Assert.assertEquals(appName, returnedApp.getAppName());
	}

	@Test
	public void testGetBuildList() throws Exception {
		VeracodeXmlApiClient client = makeDefaultTestClient();

		long buildId = 789;
		Buildlist builds = new Buildlist();
		BuildType build = new BuildType();
		build.setBuildId(buildId);
		builds.getBuild().add(build);

		String buildListXml = toXML(builds, Buildlist.class);
		WireMock.stubFor(
				WireMock.post(GETBUILDLIST).willReturn(WireMock.aResponse().withStatus(200).withBody(buildListXml)));

		Buildlist returnedBuilds = client.getBuildList("");
		Assert.assertEquals(1, returnedBuilds.getBuild().size());

		BuildType returnedBuild = returnedBuilds.getBuild().get(0);
		Assert.assertEquals(Long.valueOf(buildId), returnedBuild.getBuildId());
	}

}
