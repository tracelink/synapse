package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.sast.model.ModelType;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * Handles business logic for Apps and Mapping Apps to Projects
 * 
 * @author csmith
 *
 */
@Service
public class VeracodeSastAppService {

	private final VeracodeSastAppRepository appRepo;

	public VeracodeSastAppService(@Autowired VeracodeSastAppRepository appRepo) {
		this.appRepo = appRepo;
	}

	// App Methods
	/**
	 * get all apps that have a mapping to a Synapse project
	 * 
	 * @return a list of apps with Synapse Project Mappings
	 */
	public List<VeracodeSastAppModel> getMappedApps() {
		return appRepo.findAllBySynapseProjectNotNull();
	}

	/**
	 * get Apps that are mapped to a Synapse Project
	 * 
	 * @param synapseProject the synapse project to search on
	 * @return a list of Veracode apps that map to the given Synapse Project
	 */
	public List<VeracodeSastAppModel> getAppsBySynapseProject(ProjectModel synapseProject) {
		return appRepo.findBySynapseProject(synapseProject);
	}

	/**
	 * get all apps that have no mapping to a Synapse project
	 * 
	 * @return a list of apps with no Synapse Project Mappings
	 */
	public List<VeracodeSastAppModel> getUnmappedApps() {
		return appRepo.findAllBySynapseProjectIsNull();
	}

	public List<VeracodeSastAppModel> getAllApps() {
		return appRepo.findAll();
	}

	public VeracodeSastAppModel getSastApp(String appName, ModelType type) {
		return appRepo.findByNameAndModelType(appName, type);
	}

	public VeracodeSastAppModel save(VeracodeSastAppModel appModel) {
		return appRepo.saveAndFlush(appModel);
	}

	// Mapping Apps methods
	public void createMapping(ProjectModel project, Long appId) {
		Optional<VeracodeSastAppModel> app = appRepo.findById(appId);
		if (project != null && app.isPresent()) {
			VeracodeSastAppModel appModel = app.get();
			appModel.setSynapseProject(project);
			appRepo.saveAndFlush(appModel);
		}
	}

	public void deleteMapping(Long appId) {
		Optional<VeracodeSastAppModel> app = appRepo.findById(appId);
		if (app.isPresent()) {
			VeracodeSastAppModel appModel = app.get();
			appModel.setSynapseProject(null);
			appRepo.saveAndFlush(appModel);
		}
	}

}
