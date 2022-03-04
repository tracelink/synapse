package com.tracelink.prodsec.plugin.veracode.sast.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * Handles DB operations for the {@linkplain VeracodeSastAppModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeSastAppRepository extends JpaRepository<VeracodeSastAppModel, Long> {
	/**
	 * Gets all {@link VeracodeSastAppModel} entities where the Synapse
	 * {@link ProjectModel} is not null
	 *
	 * @return list of mapped Veracode apps
	 */
	List<VeracodeSastAppModel> findAllBySynapseProjectNotNull();

	/**
	 * Gets all {@link VeracodeSastAppModel} entities where the Synapse
	 * {@link ProjectModel} is null
	 *
	 * @return list of unmapped Veracode apps
	 */
	List<VeracodeSastAppModel> findAllBySynapseProjectIsNull();

	/**
	 * Gets the {@link VeracodeSastAppModel} for the given Synapse
	 * {@link ProjectModel}.
	 *
	 * @param synapseProject the Synapse Project to search by
	 * @return the Veracode app mapped to the Synapse Project, or null
	 */
	List<VeracodeSastAppModel> findBySynapseProject(ProjectModel synapseProject);

	/**
	 * Gets the {@link VeracodeSastAppModel} with the given name.
	 *
	 * @param name the name to search by
	 * @return the Veracode app with the given name, or null
	 */
	VeracodeSastAppModel findByName(String name);
}
