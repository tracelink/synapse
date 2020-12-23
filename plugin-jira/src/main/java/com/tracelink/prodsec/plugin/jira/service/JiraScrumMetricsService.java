package com.tracelink.prodsec.plugin.jira.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;

import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import com.tracelink.prodsec.plugin.jira.repo.JiraScrumMetricsRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles business logic for storing and retrieving scrum metrics
 *
 * @author bhoran
 */
@Service
public class JiraScrumMetricsService {

	private final JiraClientConfigService clientService;
	private final JiraScrumMetricsRepo scrumMetricsRepo;
	private final JiraPhrasesService jiraPhraseService;

	/**
	 * Creates this JiraScrumMetricsService with the pre-configured database
	 * {@link JiraScrumMetricsRepo} to interact and add {@link JiraScrumMetric}
	 * instances
	 *
	 * @param scrumMetricsRepo  the scrumMetricsRepository containing JiraScrumMetrics
	 * @param clientService     the configured client
	 * @param jiraPhraseService the jiraPhraseService to get the phrase related to these metrics
	 */
	public JiraScrumMetricsService(@Autowired JiraScrumMetricsRepo scrumMetricsRepo,
			@Autowired JiraClientConfigService clientService,
			@Autowired JiraPhrasesService jiraPhraseService) {
		this.scrumMetricsRepo = scrumMetricsRepo;
		this.clientService = clientService;
		this.jiraPhraseService = jiraPhraseService;
	}

	public void storeScrumMetrics() throws Exception {
		String scrumJqlSearchFormat = jiraPhraseService.getPhraseForData(JiraPhraseDataFormat.SCRUM);
		JiraRestClient restClient = clientService.createRestClient();

		int todo = getTotalResults(String.format(scrumJqlSearchFormat, "Backlog"), restClient);
		int prog = getTotalResults(String.format(scrumJqlSearchFormat, "\"In Progress\""),
				restClient);
		int block = getTotalResults(String.format(scrumJqlSearchFormat, "Blocked"), restClient);
		int done = getTotalResults(String.format(scrumJqlSearchFormat, "Done"), restClient);

		LocalDate currentTime = LocalDate.now();
		JiraScrumMetric scrumMetricEntity = scrumMetricsRepo.findOneByRecordedDate(currentTime);
		if (scrumMetricEntity == null) {
			scrumMetricEntity = new JiraScrumMetric();
			scrumMetricEntity.setRecordedDate(currentTime);
		}
		scrumMetricEntity.setTodo(todo);
		scrumMetricEntity.setProg(prog);
		scrumMetricEntity.setBlock(block);
		scrumMetricEntity.setDone(done);
		scrumMetricsRepo.saveAndFlush(scrumMetricEntity);
	}

	public List<JiraScrumMetric> getAllScrumMetrics() {
		return scrumMetricsRepo.findAll();
	}

	public JiraScrumMetric getMostRecent() {
		return scrumMetricsRepo.findTopByOrderByRecordedDateDesc();
	}

	public JiraScrumMetric getOldestMetric() {
		return scrumMetricsRepo.findTopByOrderByRecordedDateAsc();
	}

	private int getTotalResults(String jql, JiraRestClient restClient) throws Exception {
		return restClient.getSearchClient().searchJql(jql).get().getTotal();
	}
}
