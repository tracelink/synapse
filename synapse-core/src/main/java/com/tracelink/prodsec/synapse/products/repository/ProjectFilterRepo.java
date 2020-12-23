package com.tracelink.prodsec.synapse.products.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;

/**
 * Repository to manage operations for ProjectFilters in the database
 * 
 * @author csmith
 *
 */
@Repository
public interface ProjectFilterRepo extends JpaRepository<ProjectFilterModel, Long> {
	/**
	 * Get a ProjectFilter by its name, or null if not found
	 * 
	 * @param name the name of the ProjectFilter
	 * @return a ProjectFilter with the given name, or null if not found
	 */
	ProjectFilterModel findByName(String name);

	/**
	 * Get all ProjectFilters and order them by their name
	 * 
	 * @return a List of ProjectFilters ordered by name, or empty if there are no
	 *         ProjectFilters
	 */
	List<ProjectFilterModel> findAllByOrderByNameAsc();


}
