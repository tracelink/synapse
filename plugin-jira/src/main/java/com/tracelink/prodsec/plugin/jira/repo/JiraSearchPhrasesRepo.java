package com.tracelink.prodsec.plugin.jira.repo;

import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraPhrases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DB integration with the {@link JiraPhrases}.
 *
 * @author bhoran
 */
@Repository
public interface JiraSearchPhrasesRepo extends JpaRepository<JiraPhrases, Long> {

	List<JiraPhrases> findByDataFormat(JiraPhraseDataFormat dataFormat);
}
