package com.tracelink.prodsec.lib.veracode.xml.api;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiClient;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.veracode.apiwrapper.AbstractAPIWrapper;
import com.veracode.http.WebClient;

public class VeracodeXmlApiClientTest {

	private static final String GETBUILDLIST = "/api/5.0/getbuildlist.do";

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

	private void injectWireMock(VeracodeXmlApiClient client) throws Exception {
		List<String> wrapperFieldNames = Arrays.asList("uploadWrapper");
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
