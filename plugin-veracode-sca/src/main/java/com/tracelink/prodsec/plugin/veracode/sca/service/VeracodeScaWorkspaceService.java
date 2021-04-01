package com.tracelink.prodsec.plugin.veracode.sca.service;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaWorkspaceRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Workspace;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service to store and retrieve data about Veracode SCA workspaces from the {@link
 * VeracodeScaWorkspaceRepository}.
 *
 * @author mcool
 */
@Service
public class VeracodeScaWorkspaceService {

	private final VeracodeScaWorkspaceRepository workspaceRepository;
	private final VeracodeScaProjectService projectService;

	public VeracodeScaWorkspaceService(
			@Autowired VeracodeScaWorkspaceRepository workspaceRepository,
			@Autowired VeracodeScaProjectService projectService) {
		this.workspaceRepository = workspaceRepository;
		this.projectService = projectService;
	}

	/**
	 * Gets all {@link VeracodeScaWorkspace}s.
	 *
	 * @return list of Veracode SCA workspaces
	 */
	public List<VeracodeScaWorkspace> getWorkspaces() {
		return workspaceRepository.findAll();
	}

	/**
	 * Updates each {@link VeracodeScaWorkspace} stored in the database whose ID matches a workspace
	 * in
	 * the given list. Updates all values of the workspace model according to to the values of the
	 * given workspace.
	 *
	 * @param workspaces list of workspaces to use to update the workspace models stored in the
	 *                   database
	 * @return map from workspace ID to workspace model for all workspace models updated
	 */
	public List<VeracodeScaWorkspace> updateWorkspaces(List<Workspace> workspaces) {
		List<VeracodeScaWorkspace> workspaceModels = new ArrayList<>();
		workspaces.forEach(workspace -> {
			// Get workspace model with matching ID, or create a new one
			VeracodeScaWorkspace workspaceModel = workspaceRepository.findById(workspace.getId())
					.orElse(new VeracodeScaWorkspace());
			// Update the workspace model fields
			workspaceModel.setId(workspace.getId());
			workspaceModel.setName(workspace.getName());
			workspaceModel.setSiteId(workspace.getSiteId());
			workspaceModels.add(workspaceModel);
		});
		workspaceRepository.saveAll(workspaceModels);
		workspaceRepository.flush();
		return workspaceModels;
	}

	/**
	 * Includes all {@link VeracodeScaWorkspace}s with the given workspace IDs. Excludes any
	 * workspace whose ID is not in the list.
	 *
	 * @param workspaceIds list of IDs of the Veracode SCA workspaces to set as included
	 * @throws IllegalArgumentException if any of the workspace IDs are null
	 */
	public void setIncluded(List<UUID> workspaceIds) throws IllegalArgumentException {
		// Make sure all workspaceIds are not null
		if (workspaceIds == null || workspaceIds.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("Please provide non-null workspace IDs to include");
		}
		// For each page of workspaces, update whether or not the workspace is included and save all
		Page<VeracodeScaWorkspace> pagedWorkspaces = null;
		do {
			Pageable pageRequest = (pagedWorkspaces == null) ? PageRequest.of(0, 50)
					: pagedWorkspaces.nextPageable();
			pagedWorkspaces = workspaceRepository.findAll(pageRequest);
			pagedWorkspaces.forEach(
					workspace -> workspace.setIncluded(workspaceIds.contains(workspace.getId())));
			workspaceRepository.saveAll(pagedWorkspaces);
		} while (pagedWorkspaces.hasNext());
		// Flush before returning
		workspaceRepository.flush();
	}

	/**
	 * Deletes the {@link VeracodeScaWorkspace} with the given ID. Also deletes any {@link
	 * VeracodeScaProject} and any {@link VeracodeScaIssue} that is associated with the workspace to
	 * avoid orphaned projects and issues.
	 *
	 * @param workspaceId the ID of the workspace to delete
	 * @throws IllegalArgumentException    if the workspace ID is null
	 * @throws VeracodeScaProductException if there is no workspace with the given ID
	 */
	public void deleteWorkspace(UUID workspaceId)
			throws IllegalArgumentException, VeracodeScaProductException {
		// Make sure the workspace ID is not null
		if (workspaceId == null) {
			throw new IllegalArgumentException("Please provide a non-null workspace ID to delete");
		}
		// Make sure the workspace exists
		VeracodeScaWorkspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
		if (workspace == null) {
			throw new VeracodeScaProductException("No workspace with the given ID exists");
		}
		// Delete projects associated with the workspace
		projectService.deleteProjectsByWorkspace(workspace);
		// Delete the workspace
		workspaceRepository.delete(workspace);
		// Flush before returning
		workspaceRepository.flush();
	}
}
