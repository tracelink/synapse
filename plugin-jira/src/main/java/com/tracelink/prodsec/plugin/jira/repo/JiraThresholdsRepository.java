package com.tracelink.prodsec.plugin.jira.repo;

import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link JiraThresholds}.
 *
 * @author bhoran
 */
@Repository
public interface JiraThresholdsRepository extends JpaRepository<JiraThresholds, Long> {

}
