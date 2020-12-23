package com.tracelink.prodsec.plugin.veracode.dast.service;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for Apps and Mapping Apps to Product Lines
 *
 * @author csmith
 */
@Service
public class VeracodeDastAppService {

	private final VeracodeDastAppRepository appRepo;

	public VeracodeDastAppService(@Autowired VeracodeDastAppRepository appRepo) {
		this.appRepo = appRepo;
	}

	// App Methods

	/**
	 * get all apps that have a mapping to a Synapse product line
	 *
	 * @return a list of apps with Synapse Product Line Mappings
	 */
	public List<VeracodeDastAppModel> getMappedApps() {
		return appRepo.findAllBySynapseProductLineNotNull();
	}

	/**
	 * get Apps that are mapped to a Synapse Product Line
	 *
	 * @param synapseProductLine the synapse product line to search on
	 * @return a list of Veracode apps that map to the given Synapse Product Line
	 */
	public List<VeracodeDastAppModel> getAppsBySynapseProductLine(
			ProductLineModel synapseProductLine) {
		return appRepo.findBySynapseProductLine(synapseProductLine);
	}

	/**
	 * get all apps that have no mapping to a Synapse product line
	 *
	 * @return a list of apps with no Synapse Product Line Mappings
	 */
	public List<VeracodeDastAppModel> getUnmappedApps() {
		return appRepo.findAllBySynapseProductLineIsNull();
	}

	public List<VeracodeDastAppModel> getAllApps() {
		return appRepo.findAll();
	}

	public VeracodeDastAppModel getDastApp(String appName) {
		return appRepo.findByName(appName);
	}

	public VeracodeDastAppModel save(VeracodeDastAppModel appModel) {
		return appRepo.saveAndFlush(appModel);
	}

	// Mapping Apps methods
	public void createMapping(ProductLineModel productLine, String appName) {
		VeracodeDastAppModel app = appRepo.findByName(appName);
		if (productLine != null && app != null) {
			app.setSynapseProductLine(productLine);
			appRepo.saveAndFlush(app);
		}
	}

	public void deleteMapping(String appName) {
		VeracodeDastAppModel app = appRepo.findByName(appName);
		if (app != null) {
			app.setSynapseProductLine(null);
			appRepo.saveAndFlush(app);
		}
	}

}
