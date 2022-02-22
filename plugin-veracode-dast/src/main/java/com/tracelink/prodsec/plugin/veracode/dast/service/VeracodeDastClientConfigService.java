package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tracelink.prodsec.lib.veracode.api.VeracodeApiClient;
import com.tracelink.prodsec.lib.veracode.api.VeracodeApiException;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestPagedResourcesIterator;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Application;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourceOfApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.SummaryReport;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.api.xml.data.applist.AppType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.applist.Applist;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.Detailedreport;
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
	public VeracodeApiClient getApiClient() {
		VeracodeDastClientConfigModel config = getClientConfig();
		if (config == null) {
			return null;
		}
		VeracodeApiClient apiClient = new VeracodeApiClient("https://api.veracode.com", config.getApiId(),
				config.getApiKey());
		return apiClient;
	}

	/**
	 * Test each of the APIs we intend to use. This succeeds fast, and fails fast
	 * 
	 * @throws VeracodeApiException
	 */
	public void testAccess() throws VeracodeApiException {
		// At each api call, we can fail due to an access problem,
		// so we need to call each api
		VeracodeApiClient apiClient = getApiClient();
		VeracodeRestPagedResourcesIterator<PagedResourceOfApplication> appIterator = new VeracodeRestPagedResourcesIterator<>(
				page -> apiClient.getRestApplications(ScanTypeEnum.DYNAMIC, page));
		String restAppId;
		if (appIterator.hasNext()) {
			restAppId = String.valueOf(appIterator.next().getEmbedded().getApplications().get(0).getId());
		} else {
			throw new VeracodeApiException("Could not get applications with the REST API");
		}
		Applist apps = apiClient.getXMLApplications();
		for (AppType app : apps.getApp()) {
			String appId = String.valueOf(app.getAppId());
			Buildlist builds = apiClient.getXMLBuildList(appId);
			for (BuildType build : builds.getBuild()) {
				String buildId = String.valueOf(build.getBuildId());
				apiClient.getXMLDetailedReport(buildId);
				apiClient.getRestSummaryReport(restAppId, buildId);
				// we've called all apis and can quit now
				return;
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
