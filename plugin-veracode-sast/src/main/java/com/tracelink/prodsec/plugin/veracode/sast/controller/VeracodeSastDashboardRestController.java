package com.tracelink.prodsec.plugin.veracode.sast.controller;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.synapse.util.bucketer.SimpleBucketer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Dashboard Rest controller supports rest commands used by the dashboard to
 * populate data for graphs
 *
 * @author csmith
 */
@RestController
@RequestMapping("/veracodesast/rest")
public class VeracodeSastDashboardRestController {

	private final VeracodeSastAppService appService;

	public VeracodeSastDashboardRestController(@Autowired VeracodeSastAppService appService) {
		this.appService = appService;
	}

	@GetMapping(value = "flaws", params = {"period", "category"})
	public ResponseEntity<Map<String, List<?>>> getAllFlaws(@RequestParam String period,
			@RequestParam String category) {
		List<VeracodeSastAppModel> apps = appService.getIncludedApps();
		return getResponse(apps, period, category);
	}

	@GetMapping(value = "flaws", params = {"productLine", "period", "category"})
	public ResponseEntity<Map<String, List<?>>> getFlawsForProductLine(
			@RequestParam String productLine,
			@RequestParam String period, @RequestParam String category) {
		List<VeracodeSastAppModel> apps = appService.getMappedApps().stream()
				.filter(
						app -> app.getSynapseProject().getOwningProductLine().getName()
								.equals(productLine))
				.collect(Collectors.toList());
		return getResponse(apps, period, category);
	}

	@GetMapping(value = "flaws", params = {"projectFilter", "period", "category"})
	public ResponseEntity<Map<String, List<?>>> getFlawsForFilter(
			@RequestParam String projectFilter,
			@RequestParam String period, @RequestParam String category) {
		List<VeracodeSastAppModel> apps = appService.getMappedApps().stream()
				.filter(app -> app.getSynapseProject()
						.getFilters().stream()
						.anyMatch(filter -> filter.getName().equals(projectFilter)))
				.collect(Collectors.toList());
		return getResponse(apps, period, category);
	}

	@GetMapping(value = "flaws", params = {"project", "period", "category"})
	public ResponseEntity<Map<String, List<?>>> getFlawsForProject(@RequestParam String project,
			@RequestParam String period, @RequestParam String category) {
		List<VeracodeSastAppModel> apps = appService.getMappedApps().stream()
				.filter(app -> app.getSynapseProject().getName().equals(project))
				.collect(Collectors.toList());
		return getResponse(apps, period, category);
	}

	private ResponseEntity<Map<String, List<?>>> getResponse(List<VeracodeSastAppModel> apps,
			String period,
			String category) {
		ResponseEntity<Map<String, List<?>>> response;
		try {
			SimpleBucketer<VeracodeSastReportModel> bucketer = new SimpleBucketer<>(
					period, getOldestReportDate(apps), VeracodeSastReportModel::getReportDate);
			Map<String, List<?>> results = new LinkedHashMap<>();
			results.put("labels", bucketer.getBucketIntervals().getLabels());
			Map<String, List<Long>> datasets = getCategoryDatasets(category, apps,
					bucketer);
			datasets.forEach(results::put);
			response = ResponseEntity.ok(results);
		} catch (IllegalArgumentException e) {
			response = ResponseEntity.badRequest()
					.body(Collections
							.singletonMap("error", Collections.singletonList(e.getMessage())));
		}
		return response;
	}

	private Map<String, List<Long>> getCategoryDatasets(String category,
			List<VeracodeSastAppModel> apps, SimpleBucketer<VeracodeSastReportModel> bucketer) {
		switch (category) {
			case "severity":
				return getSeverityDatasets(apps, bucketer);
			case "cwe":
				return getCweDatasets(apps, bucketer);
			default:
				throw new IllegalArgumentException("Unknown categorization");
		}
	}

	private Map<String, List<Long>> getCweDatasets(List<VeracodeSastAppModel> apps,
			SimpleBucketer<VeracodeSastReportModel> bucketer) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		for (VeracodeSastAppModel app : apps) {
			List<List<VeracodeSastReportModel>> reports = bucketer
					.putItemsInBuckets(app.getReports());
			for (int i = 0; i < reports.size(); i++) {
				if (reports.get(i).isEmpty()) {
					continue;
				}
				VeracodeSastReportModel report = reports.get(i).get(0);
				final int j = i;
				report.getFlaws().forEach(
						f -> updateCounts(datasets, j, reports.size(), f.getCategoryName(),
								f.getCount()));
			}
		}
		return datasets;
	}

	private Map<String, List<Long>> getSeverityDatasets(List<VeracodeSastAppModel> apps,
			SimpleBucketer<VeracodeSastReportModel> bucketer) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		for (VeracodeSastAppModel app : apps) {
			List<List<VeracodeSastReportModel>> reports = bucketer
					.putItemsInBuckets(app.getReports());
			for (int i = 0; i < reports.size(); i++) {
				if (reports.get(i).isEmpty()) {
					continue;
				}
				for (VeracodeSastReportModel report : reports.get(i)) {
					updateCounts(datasets, i, reports.size(), "Very High",
							report.getVeryHighVios());
					updateCounts(datasets, i, reports.size(), "High", report.getHighVios());
					updateCounts(datasets, i, reports.size(), "Medium", report.getMedVios());
					updateCounts(datasets, i, reports.size(), "Low", report.getLowVios());
					updateCounts(datasets, i, reports.size(), "Very Low", report.getVeryLowVios());
					updateCounts(datasets, i, reports.size(), "Informational",
							report.getInfoVios());
				}
			}
		}
		return datasets;
	}

	private void updateCounts(Map<String, List<Long>> datasets, int index, int listSize, String key,
			long value) {
		if (datasets.containsKey(key)) {
			List<Long> counts = datasets.get(key);
			counts.set(index, counts.get(index) + value);
		} else {
			List<Long> counts = new ArrayList<>();
			IntStream.range(0, listSize).forEach(idx -> counts.add(0L));
			counts.set(index, value);
			datasets.put(key, counts);
		}
	}

	private Supplier<LocalDateTime> getOldestReportDate(List<VeracodeSastAppModel> apps) {
		return () -> {
			Optional<LocalDateTime> localOpt = apps.stream()
					.filter(app -> app.getOldestReport() != null)
					.map(app -> app.getOldestReport().getReportDate())
					.min(LocalDateTime::compareTo);
			return localOpt.orElseGet(LocalDateTime::now);
		};
	}

}
