package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastProductException;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * Handles business logic for Apps and Mapping Apps to Projects
 *
 * @author csmith
 */
@Service
public class VeracodeSastAppService {

	private final VeracodeSastAppRepository appRepo;
	private final VeracodeSastReportService reportService;

	public VeracodeSastAppService(@Autowired VeracodeSastAppRepository appRepo,
			@Autowired VeracodeSastReportService reportService) {
		this.appRepo = appRepo;
		this.reportService = reportService;
	}

	// App Methods

	/**
	 * Get all included apps that have a mapping to a Synapse app
	 *
	 * @return a list of apps with Synapse Project Mappings
	 */
	public List<VeracodeSastAppModel> getMappedApps() {
		return appRepo.findAllBySynapseProjectNotNull().stream()
				.filter(VeracodeSastAppModel::isIncluded).collect(Collectors.toList());
	}

	/**
	 * Get all included apps that are mapped to a Synapse Project
	 *
	 * @param synapseProject the synapse app to search on
	 * @return a list of Veracode apps that map to the given Synapse Project
	 */
	public List<VeracodeSastAppModel> getAppsBySynapseProject(ProjectModel synapseProject) {
		return appRepo.findBySynapseProject(synapseProject).stream()
				.filter(VeracodeSastAppModel::isIncluded).collect(Collectors.toList());
	}

	/**
	 * Get all included apps that have no mapping to a Synapse app
	 *
	 * @return a list of apps with no Synapse Project Mappings
	 */
	public List<VeracodeSastAppModel> getUnmappedApps() {
		return appRepo.findAllBySynapseProjectIsNull().stream()
				.filter(VeracodeSastAppModel::isIncluded).collect(Collectors.toList());
	}

	public List<VeracodeSastAppModel> getAllApps() {
		return appRepo.findAll();
	}

	/**
	 * Gets all included {@link VeracodeSastAppModel}s.
	 *
	 * @return list of included Veracode SAST apps
	 */
	public List<VeracodeSastAppModel> getIncludedApps() {
		return appRepo.findAll().stream().filter(VeracodeSastAppModel::isIncluded)
				.collect(Collectors.toList());
	}

	public VeracodeSastAppModel getSastApp(String appName) {
		return appRepo.findByName(appName);
	}

	/**
	 * Saves the given app in the {@link VeracodeSastAppRepository}.
	 *
	 * @param appModel the app to save
	 * @return the updated app
	 */
	public VeracodeSastAppModel save(VeracodeSastAppModel appModel) {
		return appRepo.saveAndFlush(appModel);
	}

	// Data management methods

	/**
	 * Includes all {@link VeracodeSastAppModel}s with the given app IDs. Excludes any app
	 * whose ID is not in the list.
	 *
	 * @param appIds list of IDs of the Veracode SAST apps to set as included
	 * @throws IllegalArgumentException if any of the given IDs are null
	 */
	public void setIncluded(List<Long> appIds) throws IllegalArgumentException {
		// Make sure all appIds are not null
		if (appIds == null || appIds.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("Please provide non-null app IDs to include");
		}
		// For each page of apps, update whether or not the app is included and save all
		Page<VeracodeSastAppModel> pagedApps = null;
		do {
			Pageable pageRequest = (pagedApps == null) ? PageRequest.of(0, 50)
					: pagedApps.nextPageable();
			pagedApps = appRepo.findAll(pageRequest);
			pagedApps.forEach(app -> app.setIncluded(appIds.contains(app.getId())));
			appRepo.saveAll(pagedApps);
		} while (pagedApps.hasNext());
		// Flush before returning
		appRepo.flush();
	}

	/**
	 * Deletes the {@link VeracodeSastAppModel} with the given ID. Also deletes any {@link
	 * VeracodeSastReportModel} that is associated with the app to
	 * avoid orphaned reports.
	 *
	 * @param appId the ID of the app to delete
	 * @throws IllegalArgumentException     if the app ID is null
	 * @throws VeracodeSastProductException if there is no app with the given ID
	 */
	public void deleteApp(Long appId)
			throws IllegalArgumentException, VeracodeSastProductException {
		// Make sure the app ID is not null
		if (appId == null) {
			throw new IllegalArgumentException("Please provide a non-null app ID to delete");
		}
		// Make sure the app exists
		VeracodeSastAppModel app = appRepo.findById(appId).orElse(null);
		if (app == null) {
			throw new VeracodeSastProductException("No app with the given ID exists");
		}
		// Delete reports associated with the app
		reportService.deleteReportsByApp(app);
		// Delete the app
		appRepo.delete(app);
		// Flush before returning
		appRepo.flush();
	}

	// Mapping Apps methods

	/**
	 * Creates a mapping between the given project and app
	 *
	 * @param project the project to map
	 * @param appId   the ID of the app to map
	 */
	public void createMapping(ProjectModel project, Long appId) {
		Optional<VeracodeSastAppModel> app = appRepo.findById(appId);
		if (project != null && app.isPresent()) {
			VeracodeSastAppModel appModel = app.get();
			appModel.setSynapseProject(project);
			appRepo.saveAndFlush(appModel);
		}
	}

	/**
	 * Deletes the mapping from the given app
	 *
	 * @param appId the ID of the app for which to remove the mapping
	 */
	public void deleteMapping(Long appId) {
		Optional<VeracodeSastAppModel> app = appRepo.findById(appId);
		if (app.isPresent()) {
			VeracodeSastAppModel appModel = app.get();
			appModel.setSynapseProject(null);
			appRepo.saveAndFlush(appModel);
		}
	}

}
