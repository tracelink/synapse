package com.tracelink.prodsec.plugin.jira.repo;

import com.tracelink.prodsec.plugin.jira.model.JiraClient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link JiraClient}.
 *
 * @author bhoran
 */
@Repository
public interface JiraClientRepository extends JpaRepository<JiraClient, Long> {

}
