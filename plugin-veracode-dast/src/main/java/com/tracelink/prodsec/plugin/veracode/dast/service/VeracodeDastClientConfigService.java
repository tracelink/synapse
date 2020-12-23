package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.dast.api.ApiClient;
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
	public ApiClient getApiClient() {
		VeracodeDastClientConfigModel config = getClientConfig();
		if (config == null) {
			return null;
		}
		ApiClient client = new ApiClient();
		client.setConfig(config);
		return client;
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
