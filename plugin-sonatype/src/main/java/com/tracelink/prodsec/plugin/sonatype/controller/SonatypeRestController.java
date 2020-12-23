package com.tracelink.prodsec.plugin.sonatype.controller;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeMetricsService;
import com.tracelink.prodsec.plugin.sonatype.util.ThreatLevel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.util.bucketer.SimpleBucketer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Sonatype rest controller handles REST requests for retrieving Sonatype
 * Nexus IQ violations data to be displayed in the graph on the Sonatype
 * dashboard page.
 *
 * @author mcool
 */
@RestController
@RequestMapping("/sonatype/rest")
public class SonatypeRestController {

	private final SonatypeAppService appService;
	private final SonatypeMetricsService metricsService;

	public SonatypeRestController(@Autowired SonatypeAppService appService,
		@Autowired SonatypeMetricsService metricsService) {
		this.appService = appService;
		this.metricsService = metricsService;
	}

	/**
	 * Gets violations metrics for a Synapse product line over time,
	 * separated by project.
	 *
	 * @param productLine the product line to gather metrics for
	 * @param period      the time period over which metrics should be gathered
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/violations", params = {"productLine", "period"})
	public ResponseEntity<Map<String, List<?>>> getViolationsForProductLine(
		@RequestParam String productLine,
		@RequestParam String period) {
		// Get Sonatype apps mapped to the given product line
		List<SonatypeApp> mappedApps = appService.getMappedApps().stream()
			.filter(a -> a.getSynapseProject().getOwningProductLine().getName().equals(productLine))
			.collect(Collectors.toList());

		return getViolationsHelper(mappedApps, period);
	}

	/**
	 * Gets violations metrics for a Synapse project filter over time,
	 * separated by project.
	 *
	 * @param projectFilter the project filter to gather metrics for
	 * @param period        the time period over which metrics should be gathered
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/violations", params = {"projectFilter", "period"})
	public ResponseEntity<Map<String, List<?>>> getViolationsForProjectFilter(
		@RequestParam String projectFilter,
		@RequestParam String period) {
		// Get Sonatype apps mapped to the given project filter
		List<SonatypeApp> mappedApps = appService
			.getMappedApps().stream().filter(a -> a.getSynapseProject().getFilters().stream()
				.map(ProjectFilterModel::getName).collect(Collectors.toList())
				.contains(projectFilter))
			.collect(Collectors.toList());

		return getViolationsHelper(mappedApps, period);
	}

	/**
	 * Gets violations metrics for a single Synapse project over time.
	 *
	 * @param project the project to gather metrics for
	 * @param period  the time period over which metrics should be gathered
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/violations", params = {"project", "period"})
	public ResponseEntity<Map<String, List<?>>> getViolationsForProject(
		@RequestParam String project,
		@RequestParam String period) {
		// Get Sonatype apps mapped to the given project
		List<SonatypeApp> mappedApps = appService.getMappedApps().stream()
			.filter(a -> a.getSynapseProject().getName().equals(project))
			.collect(Collectors.toList());

		return getViolationsHelper(mappedApps, period);
	}

	private ResponseEntity<Map<String, List<?>>> getViolationsHelper(List<SonatypeApp> apps,
		String period) {
		SimpleBucketer<SonatypeMetrics> bucketer;
		try {
			bucketer = new SimpleBucketer<>(period,
				() -> metricsService.getEarliestMetricsDate().atStartOfDay(),
				m -> m.getRecordedDate().atStartOfDay());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(Collections.singletonMap("error", Collections.singletonList(e.getMessage())));
		}

		Map<String, List<?>> results = new LinkedHashMap<>();
		results.put("labels", bucketer.getBucketIntervals().getLabels());
		Map<String, List<Long>> datasets = getSeverityDatasets(apps, bucketer);
		datasets.forEach(results::put);
		return ResponseEntity.ok(results);
	}

	private Map<String, List<Long>> getSeverityDatasets(List<SonatypeApp> apps,
		SimpleBucketer<SonatypeMetrics> bucketer) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		for (SonatypeApp app : apps) {
			// Get all metrics for this app and bucket them
			List<List<SonatypeMetrics>> metrics = bucketer.putItemsInBuckets(app.getMetrics());

			for (int i = 0; i < metrics.size(); i++) {
				if (metrics.get(i).isEmpty()) {
					continue;
				}
				SonatypeMetrics m = metrics.get(i).get(0);

				for (ThreatLevel tl : ThreatLevel.values()) {
					String name = tl.getName();
					if (datasets.containsKey(name)) {
						List<Long> counts = datasets.get(name);
						counts.set(i, counts.get(i) + tl.getViosCallback().applyAsLong(m));
					} else {
						List<Long> counts = new ArrayList<>();
						IntStream.range(0, metrics.size()).forEach(index -> counts.add(0L));
						counts.set(i, tl.getViosCallback().applyAsLong(m));
						datasets.put(name, counts);
					}
				}
			}
		}
		return datasets;
	}

}
