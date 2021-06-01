package com.tracelink.prodsec.plugin.veracode.sca.repository;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for DB integration with the {@link VeracodeScaProject}.
 *
 * @author mcool
 */
@Repository
public interface VeracodeScaProjectRepository extends JpaRepository<VeracodeScaProject, UUID> {

	/**
	 * Gets all {@link VeracodeScaProject} entities where the Synapse {@link
	 * ProjectModel} is not null
	 *
	 * @return list of mapped Veracode SCA apps
	 */
	List<VeracodeScaProject> findAllBySynapseProjectNotNull();

	/**
	 * Gets all {@link VeracodeScaProject} entities where the Synapse {@link
	 * ProjectModel} is null
	 *
	 * @return list of unmapped Veracode SCA apps
	 */
	List<VeracodeScaProject> findAllBySynapseProjectIsNull();

	/**
	 * Gets the list of {@link VeracodeScaProject} for the given Synapse {@link ProjectModel}.
	 * This does a search by the join column automatically.
	 *
	 * @param synapseProject the Synapse project to search by
	 * @return the Veracode SCA project mapped to the Synapse project, or null
	 */
	List<VeracodeScaProject> findBySynapseProject(ProjectModel synapseProject);

	/**
	 * Gets the {@link VeracodeScaProject} with the given name.
	 *
	 * @param name the name to search by
	 * @return the Veracode SCA project with the given name, or null
	 */
	VeracodeScaProject findByName(String name);

	/**
	 * Gets the list of {@link VeracodeScaProject}s whose IDs are in the given list.
	 *
	 * @param ids list of IDs of projects to get
	 * @return list of projects with the given IDs
	 */
	List<VeracodeScaProject> findByIdIn(List<UUID> ids);

	/**
	 * Gets a page of {@link VeracodeScaProject}s that are associated with the given {@link
	 * VeracodeScaWorkspace}.
	 *
	 * @param workspace workspace for which to get projects
	 * @param pageable  the page information for the database request
	 * @return page of projects associated with the given workspace
	 */
	Page<VeracodeScaProject> findAllByWorkspace(VeracodeScaWorkspace workspace, Pageable pageable);

	/**
	 * Deletes all projects associated with the given {@link VeracodeScaWorkspace}.
	 *
	 * @param workspace the workspace for which to delete all projects
	 */
	@Transactional
	void deleteByWorkspace(VeracodeScaWorkspace workspace);
}
