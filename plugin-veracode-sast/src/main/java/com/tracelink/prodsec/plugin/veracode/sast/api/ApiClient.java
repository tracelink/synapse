package com.tracelink.prodsec.plugin.veracode.sast.api;

import com.tracelink.prodsec.plugin.veracode.sast.api.data.applist.AppType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.applist.Applist;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.buildlist.BuildType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.sandboxlist.SandboxType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.sandboxlist.Sandboxlist;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastClientConfigModel;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.SandboxAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.util.Assert;

/**
 * A client implementation to wrap the Veracode API and produce reasonable
 * POJOs.
 *
 * @author csmith
 */
public class ApiClient {

	private final UploadAPIWrapper uploadWrapper = new UploadAPIWrapper();
	private final SandboxAPIWrapper sandboxWrapper = new SandboxAPIWrapper();
	private final ResultsAPIWrapper resultsWrapper = new ResultsAPIWrapper();

	public void setConfig(VeracodeSastClientConfigModel config) {
		Assert.notNull(config, "Client Config is null");

		uploadWrapper.setUpApiCredentials(config.getApiId(), config.getApiKey());
		sandboxWrapper.setUpApiCredentials(config.getApiId(), config.getApiKey());
		resultsWrapper.setUpApiCredentials(config.getApiId(), config.getApiKey());
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
	 * @throws VeracodeClientException if any API throws this Exception
	 */
	public void testAccess() throws VeracodeClientException {
		// At each api call, we can fail due to an access problem,
		// so we need to call each api
		Applist apps = this.getApplications();
		for (AppType app : apps.getApp()) {
			String appId = String.valueOf(app.getAppId());
			Sandboxlist sandboxes = this.getSandboxes(appId);
			for (SandboxType sandbox : sandboxes.getSandbox()) {
				String sbxId = String.valueOf(sandbox.getSandboxId());
				Buildlist builds = this.getBuildList(appId, sbxId);
				for (BuildType build : builds.getBuild()) {
					String buildId = String.valueOf(build.getBuildId());
					Detailedreport report = this.getDetailedReport(buildId);
					if (report != null) {
						// at this point we've tested all api calls and can quit
						return;
					}
				}
			}
		}
		throw new VeracodeClientException("Client has access but can't see any reports");
	}

	/**
	 * Get the Detailed Report of this build from Veracode
	 *
	 * @param buildId the build ID for this report
	 * @return a Report of what happened during this build
	 * @throws VeracodeClientException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Detailedreport getDetailedReport(String buildId) throws VeracodeClientException {
		Detailedreport report;
		String message;
		try {
			message = this.resultsWrapper.detailedReport(buildId);
			report = translate(message, Detailedreport.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeClientException(
					"Could not get detailed report for buildid " + buildId, e);
		}
		return report;
	}

	/**
	 * Get the list of Sandboxes for this application
	 *
	 * @param appId the application id to query for sandboxes
	 * @return a Sandbox list object for this app id
	 * @throws VeracodeClientException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Sandboxlist getSandboxes(String appId) throws VeracodeClientException {
		Sandboxlist sbxList;
		String message = null;
		try {
			message = this.sandboxWrapper.getSandboxList(appId);
			sbxList = translate(message, Sandboxlist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeClientException(
					"Could not get sandbox list for appid " + appId + " with message " + message,
					e);
		}
		return sbxList;
	}

	/**
	 * get an App list of all applications visible to this API Client
	 *
	 * @return an App list object for this client
	 * @throws VeracodeClientException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Applist getApplications() throws VeracodeClientException {
		Applist appList;
		String message;
		try {
			message = this.uploadWrapper.getAppList();
			appList = translate(message, Applist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeClientException("Could not get apps list", e);
		}
		return appList;
	}

	/**
	 * get a list of builds for this application/sandbox combo
	 *
	 * @param appId     the application id to search by
	 * @param sandboxId the sandbox to search by
	 * @return a Build list object for this appid and sandboxid
	 * @throws VeracodeClientException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Buildlist getBuildList(String appId, String sandboxId) throws VeracodeClientException {
		Buildlist buildList;
		String message;
		try {
			message = this.uploadWrapper.getBuildList(appId, sandboxId);
			buildList = translate(message, Buildlist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeClientException(
					"Could not get build list for appid " + appId + " and sandboxid " + sandboxId,
					e);
		}
		return buildList;
	}

	/**
	 * get a list of builds for this application
	 *
	 * @param appId the application id to search by
	 * @return a Build list object for this appid
	 * @throws VeracodeClientException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Buildlist getBuildList(String appId) throws VeracodeClientException {
		Buildlist buildList;
		String message;
		try {
			message = this.uploadWrapper.getBuildList(appId);
			buildList = translate(message, Buildlist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeClientException(
					"Could not get build list for appid " + appId, e);
		}
		return buildList;
	}
}
