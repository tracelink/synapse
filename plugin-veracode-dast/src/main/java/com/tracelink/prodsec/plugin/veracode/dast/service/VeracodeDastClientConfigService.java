package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tracelink.prodsec.lib.veracode.xml.api.VeracodeXmlApiClient;
import com.tracelink.prodsec.lib.veracode.xml.api.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.xml.api.data.applist.AppType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.applist.Applist;
import com.tracelink.prodsec.lib.veracode.xml.api.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.lib.veracode.xml.api.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastClientConfigModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastClientConfigRepository;

/**
 * Handles business logic for CRU operations on API Client Configuration
 * 
 * @author csmith
 *
 */
@Service
public class VeracodeDastClientConfigService {

	private final VeracodeDastClientConfigRepository clientConfigRepo;

	public VeracodeDastClientConfigService(VeracodeDastClientConfigRepository clientConfigRepo) {
		this.clientConfigRepo = clientConfigRepo;
	}

	/**
	 * get the current config to talk to Veracode
	 * 
	 * @return the api configuration, or null if none exists
	 */
	public VeracodeDastClientConfigModel getClientConfig() {
		List<VeracodeDastClientConfigModel> clients = clientConfigRepo.findAll();
		if (clients.isEmpty()) {
			return null;
		}
		return clients.get(0);
	}

	/**
	 * get a configured client to talk to Veracode
	 * 
	 * @return an api client, pre-configured with the current config. Or null if no
	 *         config exists
	 */
	public VeracodeXmlApiClient getApiClient() {
		VeracodeDastClientConfigModel config = getClientConfig();
		if (config == null) {
			return null;
		}
		VeracodeXmlApiClient xmlApiClient = new VeracodeXmlApiClient(config.getApiId(), config.getApiKey());
		return xmlApiClient;
	}
	
	/**
	 * Test each of the APIs we intend to use. This succeeds fast, and fails fast
	 *
	 * @throws VeracodeXmlApiException if any API throws this Exception
	 */
	public void testAccess() throws VeracodeXmlApiException {
		// At each api call, we can fail due to an access problem,
		// so we need to call each api
		VeracodeXmlApiClient xmlApiClient = getApiClient();
		Applist apps = xmlApiClient.getApplications();
		for (AppType app : apps.getApp()) {
			String appId = String.valueOf(app.getAppId());
			Buildlist builds = xmlApiClient.getBuildList(appId);
			for (BuildType build : builds.getBuild()) {
				String buildId = String.valueOf(build.getBuildId());
				Detailedreport report = xmlApiClient.getDetailedReport(buildId);
				if (report != null) {
					// at this point we've tested all api calls and can quit
					return;
				}
			}
		}
		throw new VeracodeXmlApiException("Client has access but can't see any reports");
	}

	/**
	 * set a new API configuration
	 * 
	 * @param apiId  the API ID of this configuration, may not be null
	 * @param apiKey the API Key of this configuration, may not be null
	 * @return the saved Configuration
	 */
	public VeracodeDastClientConfigModel setClientConfig(String apiId, String apiKey) {
		VeracodeDastClientConfigModel config = getClientConfig();
		if (config == null) {
			config = new VeracodeDastClientConfigModel();
		}

		config.setApiId(apiId);
		config.setApiKey(apiKey);
		return clientConfigRepo.saveAndFlush(config);
	}

}
