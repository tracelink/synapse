package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.model.JiraScrumMetric;
import com.tracelink.prodsec.plugin.jira.service.JiraScrumMetricsService;
import com.tracelink.prodsec.synapse.util.bucketer.SimpleBucketer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The JiraScrumMetricsRestController supports rest commands used by the Scrum Metrics
 * dashboard to populate data for graphs
 *
 * @author bhoran
 */
@RestController
@RequestMapping("/jira/rest")
public class JiraScrumMetricsRestController {

	private final JiraScrumMetricsService scrumMetricsService;

	public JiraScrumMetricsRestController(@Autowired JiraScrumMetricsService scrumMetricsService) {
		this.scrumMetricsService = scrumMetricsService;
	}

	@GetMapping(value = "/metrics/scrum", params = {"period"})
	public ResponseEntity<Map<String, List<?>>> getAllScrumMetrics(@RequestParam String period) {
		List<JiraScrumMetric> scrumMetric = scrumMetricsService.getAllScrumMetrics();
		return getScrumResponse(scrumMetric, period);
	}

	private ResponseEntity<Map<String, List<?>>> getScrumResponse(
			List<JiraScrumMetric> scrumMetrics,
			String period) {
		ResponseEntity<Map<String, List<?>>> response;
		try {
			SimpleBucketer<JiraScrumMetric> bucketer = new SimpleBucketer<>(period,
					() -> {
						JiraScrumMetric oldestMetric = scrumMetricsService.getOldestMetric();
						if (oldestMetric == null) {
							return LocalDateTime.now();
						} else {
							return oldestMetric.getRecordedDate().atStartOfDay();
						}
					},
					metric -> metric.getRecordedDate().atStartOfDay());
			Map<String, List<?>> results = new LinkedHashMap<>();
			results.put("labels", bucketer.getBucketIntervals().getLabels());
			Map<String, List<Long>> datasets = getScrumMetricDatasets(scrumMetrics, bucketer);
			datasets.forEach(results::put);
			response = ResponseEntity.ok(results);
		} catch (IllegalArgumentException e) {
			response = ResponseEntity.badRequest()
					.body(Collections
							.singletonMap("error", Collections.singletonList(e.getMessage())));
		}
		return response;
	}

	private Map<String, List<Long>> getScrumMetricDatasets(List<JiraScrumMetric> scrumMetricList,
			SimpleBucketer<JiraScrumMetric> bucketer) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		// Creating list to represent Issues
		List<Long> todoIssues = new ArrayList<>();
		List<Long> inprogressIssues = new ArrayList<>();
		List<Long> blockedIssues = new ArrayList<>();
		List<Long> finishedIssues = new ArrayList<>();

		List<List<JiraScrumMetric>> buckets = bucketer.putItemsInBuckets(scrumMetricList);

		// Iterates through each bucket
		for (List<JiraScrumMetric> bucket : buckets) {
			if (bucket.isEmpty()) {
				todoIssues.add(0L);
				inprogressIssues.add(0L);
				blockedIssues.add(0L);
				finishedIssues.add(0L);
				continue;
			}
			/* Get the contents of the current bucket and iterate through to perform a reverse sort,
			 * in order to find the most recent metric */
			JiraScrumMetric newestMetric = bucket.stream()
					.min((metric1, metric2) -> metric2.getRecordedDate()
							.compareTo(metric1.getRecordedDate())).get();

			todoIssues.add(newestMetric.getTodo());
			inprogressIssues.add(newestMetric.getProg());
			blockedIssues.add(newestMetric.getBlock());
			finishedIssues.add(newestMetric.getDone());
		}

		datasets.put("To Do", todoIssues);
		datasets.put("In Progress", inprogressIssues);
		datasets.put("Blocked", blockedIssues);
		datasets.put("Finished", finishedIssues);
		return datasets;
	}
}
