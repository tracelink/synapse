package com.tracelink.prodsec.plugin.sme.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.sme.model.SMEEntity;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

@Repository
public interface SMERepo extends JpaRepository<SMEEntity, Long> {
	SMEEntity findByName(String name);

	/**
	 * Get a list of SMEs where the Project is assigned to the SME
	 * 
	 * @param project the project to search for
	 * @return a list of SMEs assigned to this project
	 */
	List<SMEEntity> findByProjects(ProjectModel project);
}
