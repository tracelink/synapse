package com.tracelink.prodsec.plugin.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.demo.model.DemoProjectEntity;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * Repository to handle DB integration
 * 
 * @author csmith
 *
 */
@Repository
public interface DemoRepo extends JpaRepository<DemoProjectEntity, Long> {
	/**
	 * Get the {@link DemoProjectEntity} for a Synapse Project. This does a search
	 * by the join column automatically
	 * 
	 * @param project the Synapse Project to search by
	 * @return a Demo Project configured for this Synapse Project, or null
	 */
	DemoProjectEntity findBySynapseProject(ProjectModel project);
}
