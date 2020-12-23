package com.tracelink.prodsec.plugin.jira.repo;

import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * RRepository for DB integration with the {@link JiraScrumMetric}.
 *
 * @author bhoran
 */
@Repository
public interface JiraScrumMetricsRepo extends JpaRepository<JiraScrumMetric, Long> {

	JiraScrumMetric findOneByRecordedDate(LocalDate date);

	JiraScrumMetric findTopByOrderByRecordedDateAsc();

	JiraScrumMetric findTopByOrderByRecordedDateDesc();
}
