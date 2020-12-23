package com.tracelink.prodsec.plugin.jira.repo;

import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DB integration with the {@link JiraVuln}.
 *
 * @author bhoran
 */
@Repository
public interface JiraVulnMetricsRepo extends JpaRepository<JiraVuln, Long> {

	JiraVuln findTopByOrderByCreatedAsc();

	JiraVuln findTopByOrderByCreatedDesc();

	List<JiraVuln> findAllByProductLine(ProductLineModel productLine);

	List<JiraVuln> findAllByResolvedIsNull();
}
