package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tracelink.prodsec.lib.veracode.api.VeracodeApiClient;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastClientConfigModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastClientConfigRepository;

/**
 * Handles business logic for CRU operations on API Client Configuration
 * 
 * @author csmith
 *
 */
@Service
public class VeracodeSastClientConfigService {

	private final VeracodeSastClientConfigRepository clientConfigRepo;

	public VeracodeSastClientConfigService(VeracodeSastClientConfigRepository clientConfigRepo) {
		this.clientConfigRepo = clientConfigRepo;
	}

	/**
	 * get the current config to talk to Veracode
	 * 
	 * @return the api configuration, or null if none exists
	 */
	public VeracodeSastClientConfigModel getClientConfig() {
		List<VeracodeSastClientConfigModel> clients = clientConfigRepo.findAll();
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
		VeracodeSastClientConfigModel config = getClientConfig();
		if (config == null) {
			return null;
		}
		VeracodeApiClient client = new VeracodeApiClient("https://api.veracode.com", config.getApiId(),
				config.getApiKey());
		return client;
	}

	/**
	 * set a new API configuration
	 * 
	 * @param apiId  the API ID of this configuration, may not be null
	 * @param apiKey the API Key of this configuration, may not be null
	 * @return the saved Configuration
	 */
	public VeracodeSastClientConfigModel setClientConfig(String apiId, String apiKey) {
		VeracodeSastClientConfigModel config = getClientConfig();
		if (config == null) {
			config = new VeracodeSastClientConfigModel();
		}

		config.setApiId(apiId);
		config.setApiKey(apiKey);
		return clientConfigRepo.saveAndFlush(config);
	}

}
