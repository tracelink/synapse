package com.tracelink.prodsec.synapse.products.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * Repository to manage operations for Projects in the database
 * 
 * @author csmith
 *
 */
@Repository
public interface ProjectRepo extends JpaRepository<ProjectModel, Long> {
	/**
	 * Get a Project by its name, or null if not found
	 * 
	 * @param name the name of the Project
	 * @return a Project with the given name, or null if not found
	 */
	ProjectModel findByName(String name);

	/**
	 * Get all Projects and order them by their name
	 * 
	 * @return a List of Projects ordered by name, or empty if there are no Projects
	 */
	List<ProjectModel> findAllByOrderByNameAsc();
}
