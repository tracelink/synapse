package com.tracelink.prodsec.lib.veracode.xml.api;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.tracelink.prodsec.lib.veracode.xml.api.data.applist.AppType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.applist.Applist;
import com.tracelink.prodsec.lib.veracode.xml.api.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.lib.veracode.xml.api.data.detailedreport.Detailedreport;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.SandboxAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;


/**
 * A client implementation to wrap the Veracode API and produce reasonable
 * POJOs.
 *
 * @author csmith
 */
public class VeracodeXmlApiClient {

	private static final String FLAW_XML_STRING = "<flaw ";
	private static final int MAX_FLAWS = 500;
	private UploadAPIWrapper uploadWrapper = new UploadAPIWrapper();
	private SandboxAPIWrapper sandboxWrapper = new SandboxAPIWrapper();
	private ResultsAPIWrapper resultsWrapper = new ResultsAPIWrapper();


	public VeracodeXmlApiClient(String apiID, String apiKey) {
		uploadWrapper.setUpApiCredentials(apiID, apiKey);
		sandboxWrapper.setUpApiCredentials(apiID, apiKey);
		resultsWrapper.setUpApiCredentials(apiID, apiKey);
	}

	private <T> T translate(String input, Class<T> target)
			throws JAXBException, XMLStreamException {
		JAXBContext jaxb = JAXBContext.newInstance(target);

		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(input));

		Unmarshaller unmarshaller = jaxb.createUnmarshaller();
		Object result = unmarshaller.unmarshal(xsr);
		return target.cast(result);
	}

	/**
	 * Test each of the APIs we intend to use. This succeeds fast, and fails fast
	 *
	 * @throws VeracodeXmlApiException if any API throws this Exception
	 */
	public void testAccess() throws VeracodeXmlApiException {
		// At each api call, we can fail due to an access problem,
		// so we need to call each api
		Applist apps = this.getApplications();
		for (AppType app : apps.getApp()) {
			String appId = String.valueOf(app.getAppId());
			Buildlist builds = this.getBuildList(appId);
			for (BuildType build : builds.getBuild()) {
				String buildId = String.valueOf(build.getBuildId());
				Detailedreport report = this.getDetailedReport(buildId);
				if (report != null) {
					// at this point we've tested all api calls and can quit
					return;
				}
			}
		}
		throw new VeracodeXmlApiException("Client has access but can't see any reports");
	}

	/**
	 * Get the Detailed Report of this build from Veracode
	 *
	 * @param buildId the build ID for this report
	 * @return a Report of what happened during this build
	 * @throws VeracodeXmlApiException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Detailedreport getDetailedReport(String buildId) throws VeracodeXmlApiException {
		Detailedreport report;
		String message;
		try {
			message = this.resultsWrapper.detailedReport(buildId);
			int numFlaws = countOccurrencesOf(message, FLAW_XML_STRING);
			if (numFlaws > MAX_FLAWS) {
				throw new VeracodeXmlApiException(String.format(
						"Cannot parse detailed report for buildid %s because it contains %d flaws",
						buildId, numFlaws));
			}
			report = translate(message, Detailedreport.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeXmlApiException(
					"Could not get detailed report for buildid " + buildId, e);
		}
		return report;
	}
	
	private static int countOccurrencesOf(String str, String sub) {
		if (StringUtils.isBlank(str) || StringUtils.isBlank(sub)) {
			return 0;
		}

		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	/**
	 * get an App list of all applications visible to this API Client
	 *
	 * @return an App list object for this client
	 * @throws VeracodeXmlApiException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Applist getApplications() throws VeracodeXmlApiException {
		Applist appList;
		String message;
		try {
			message = this.uploadWrapper.getAppList();
			appList = translate(message, Applist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeXmlApiException("Could not get apps list", e);
		}
		return appList;
	}

	/**
	 * get a list of builds for this application/sandbox combo
	 *
	 * @param appId the application id to search by
	 * @return a Build list object for this appid and sandboxid
	 * @throws VeracodeXmlApiException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Buildlist getBuildList(String appId) throws VeracodeXmlApiException {
		Buildlist buildList;
		String message;
		try {
			message = this.uploadWrapper.getBuildList(appId);
			buildList = translate(message, Buildlist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeXmlApiException("Could not get build list for appid " + appId, e);
		}
		return buildList;
	}
}
