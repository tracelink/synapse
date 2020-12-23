package com.tracelink.prodsec.plugin.jira.repo;

import com.tracelink.prodsec.plugin.jira.model.JiraAllowedSla;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link JiraAllowedSla}.
 *
 * @author bhoran
 */
@Repository
public interface JiraAllowedSlaRepo extends JpaRepository<JiraAllowedSla, Long> {

	JiraAllowedSla findOneBySeverityEquals(String severity);
}
