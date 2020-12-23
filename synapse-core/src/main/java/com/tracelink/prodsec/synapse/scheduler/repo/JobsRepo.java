package com.tracelink.prodsec.synapse.scheduler.repo;

import com.tracelink.prodsec.synapse.scheduler.model.JobsModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage operations for Jobs in the database
 *
 * @author bhoran
 */
@Repository
public interface JobsRepo extends JpaRepository<JobsModel, Long> {

	/**
	 * Get a Job by its name, or null if not found
	 *
	 * @param name the name of the Job
	 * @return a JobModel with the given name, or null if not found
	 */
	JobsModel findByPluginJobName(String name);

	/**
	 * Get all Jobs and order them by their Plugin name
	 *
	 * @return a List of Jobs ordered by name, or empty if there are no
	 * Jobs
	 */
	List<JobsModel> findAllByOrderByPluginJobNameAsc();

}
