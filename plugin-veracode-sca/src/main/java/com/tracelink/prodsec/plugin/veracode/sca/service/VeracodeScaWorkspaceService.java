package com.tracelink.prodsec.plugin.veracode.sca.service;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaWorkspaceRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Workspace;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

	public VeracodeScaWorkspaceService(
			@Autowired VeracodeScaWorkspaceRepository workspaceRepository) {
		this.workspaceRepository = workspaceRepository;
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
	 * Excludes or includes the {@link VeracodeScaWorkspace} associated with the given workspace
	 * name. If the workspace does not exist, throws an exception.
	 *
	 * @param workspaceName the name of the Veracode SCA workspace to set the default branch for
	 * @param included      whether to include the projects and issues of this workspace
	 * @throws VeracodeScaProductException if the workspace does not exist
	 */
	public void setIncluded(String workspaceName, boolean included)
			throws VeracodeScaProductException {
		VeracodeScaWorkspace workspace = workspaceRepository.findByName(workspaceName);
		if (workspace == null) {
			throw new VeracodeScaProductException(
					"No workspace found with the name: " + workspaceName);
		}
		workspace.setIncluded(included);
		workspaceRepository.saveAndFlush(workspace);
	}
}
