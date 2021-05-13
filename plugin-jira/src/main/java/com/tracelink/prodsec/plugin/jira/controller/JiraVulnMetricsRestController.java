package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
import com.tracelink.prodsec.synapse.util.bucketer.StandardIntervalBucketer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The VulnMetricsRestController supports rest commands used by the VulnMetricsController to
 * populate data for graphs
 *
 * @author bhoran
 */
@RestController
@RequestMapping("/jira/rest")
public class JiraVulnMetricsRestController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JiraVulnMetricsRestController.class);
	private final JiraVulnMetricsService vulnMetricsService;


	public JiraVulnMetricsRestController(@Autowired JiraVulnMetricsService vulnMetricsService) {
		this.vulnMetricsService = vulnMetricsService;
	}

	/* The JiraVulnBucketer is created to ensure vulnerabilities will be displayed in the correct bucket
	 * based on when they are created and resolved, not just when the data is recorded */
	private static class JiraVulnBucketer extends StandardIntervalBucketer<JiraVuln> {

		JiraVulnBucketer(String timePeriod,
				Supplier<LocalDateTime> earliestDateTimeSupplier) {
			super(timePeriod, earliestDateTimeSupplier);
		}

		@Override
		public boolean itemBelongsInBucket(JiraVuln item, LocalDateTime bucketStart,
				LocalDateTime bucketEnd) {
			boolean inBucket = true;
			/* Ensures items created after the bucket end do not appear within the bucket.*/
			if (item.getCreated().atStartOfDay().compareTo(bucketEnd) > 0) {
				inBucket = false;
			}/* Ensures items resolved before the start of bucket are not counted. Null check prevents
			 * Null Pointer Exception from informational issues that don't have a required timeline
			 * to fix */
			if (item.getResolved() != null) {
				if (item.getResolved().atStartOfDay().compareTo(bucketStart) < 0) {
					inBucket = false;
				}
			}
			return inBucket;
		}
	}

	@GetMapping(value = "vulnmetrics", params = {"period"})
	public ResponseEntity<Map<String, List<?>>> getAllVulns(@RequestParam String period) {
		List<JiraVuln> vulnMetric = vulnMetricsService.getAllVulnMetrics();
		return getVulnResponse(vulnMetric, period);
	}

	private ResponseEntity<Map<String, List<?>>> getVulnResponse(List<JiraVuln> vulnMetrics,
			String period) {
		ResponseEntity<Map<String, List<?>>> response;
		try {
			JiraVulnBucketer bucketer = new JiraVulnBucketer(period,
					() -> {
						JiraVuln oldestMetric = vulnMetricsService.getOldestMetrics();
						if (oldestMetric == null) {
							return LocalDateTime.now();
						} else {
							return oldestMetric.getCreated().atStartOfDay();
						}
					});
			Map<String, List<?>> results = new LinkedHashMap<>();
			results.put("labels", bucketer.getBucketIntervals().getLabels());
			Map<String, List<Long>> datasets = getVulnMetricDatasets(vulnMetrics, bucketer);
			datasets.forEach(results::put);
			response = ResponseEntity.ok(results);
		} catch (IllegalArgumentException e) {
			response = ResponseEntity.badRequest()
					.body(Collections
							.singletonMap("error", Collections.singletonList(e.getMessage())));
		}
		return response;
	}

	private Map<String, List<Long>> getVulnMetricDatasets(List<JiraVuln> vulnMetricList,
			JiraVulnBucketer bucketer) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		// Creating issues type count lists
		List<Long> criticalIssues = new ArrayList<>();
		List<Long> highIssues = new ArrayList<>();
		List<Long> mediumIssues = new ArrayList<>();
		List<Long> lowIssues = new ArrayList<>();
		List<Long> informationalIssues = new ArrayList<>();
		List<Long> unknownSeverityIssues = new ArrayList<>();

		List<List<JiraVuln>> buckets = bucketer.putItemsInBuckets(vulnMetricList);

		// Iterates through each bucket
		for (List<JiraVuln> metricsInBucket : buckets) {
			long critical = 0L;
			long high = 0L;
			long medium = 0L;
			long low = 0L;
			long informational = 0L;
			long unknown = 0L;

			for (JiraVuln v : metricsInBucket) {
				String severity = v.getSev().toLowerCase();
				switch (severity) {
					case "critical":
						critical++;
						break;
					case "high":
						high++;
						break;
					case "medium":
						medium++;
						break;
					case "low":
						low++;
						break;
					case "informational":
						informational++;
						break;
					case "unknown":
						unknown++;
						break;
					default:
						LOGGER.error("Unexpected value " + severity
								+ " returned as issue severity for issue " + v.getId());
						break;
				}
			}

			criticalIssues.add(critical);
			highIssues.add(high);
			mediumIssues.add(medium);
			lowIssues.add(low);
			informationalIssues.add(informational);
			unknownSeverityIssues.add(unknown);
		}

		datasets.put("Critical", criticalIssues);
		datasets.put("High", highIssues);
		datasets.put("Medium", mediumIssues);
		datasets.put("Low", lowIssues);
		datasets.put("Informational", informationalIssues);
		datasets.put("Unknown", unknownSeverityIssues);
		return datasets;
	}
}
