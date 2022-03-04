package com.tracelink.prodsec.plugin.veracode.dast.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
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

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.util.bucketer.SimpleBucketer;

/**
 * The Dashboard Rest controller supports rest commands used by the dashboard to
 * populate data for graphs
 *
 * @author csmith
 */
@RestController
@RequestMapping("/veracodedast/rest")
public class VeracodeDastDashboardRestController {

	private final VeracodeDastAppService appService;

	public VeracodeDastDashboardRestController(@Autowired VeracodeDastAppService appService) {
		this.appService = appService;
	}

	@GetMapping(value = "reports", params = { "period", "category" })
	public ResponseEntity<Map<String, List<?>>> getAllStats(@RequestParam String period,
			@RequestParam String category) {
		List<VeracodeDastAppModel> apps = appService.getAllApps();
		return getResponse(apps, period, category);
	}

	@GetMapping(value = "reports", params = { "productLine", "period", "category" })
	public ResponseEntity<Map<String, List<?>>> getStatsForProductLine(@RequestParam String productLine,
			@RequestParam String period, @RequestParam String category) {
		List<VeracodeDastAppModel> apps = appService.getMappedApps().stream()
				.filter(app -> app.getSynapseProductLine().getName().equals(productLine)).collect(Collectors.toList());
		return getResponse(apps, period, category);
	}

	private ResponseEntity<Map<String, List<?>>> getResponse(List<VeracodeDastAppModel> apps, String period,
			String category) {
		ResponseEntity<Map<String, List<?>>> response;
		try {
			SimpleBucketer<VeracodeDastReportModel> bucketer = new SimpleBucketer<>(period, getOldestReportDate(apps),
					VeracodeDastReportModel::getReportDate);
			Map<String, List<?>> results = new LinkedHashMap<>();
			results.put("labels", bucketer.getBucketIntervals().getLabels());
			Map<String, List<Number>> datasets = getCategoryDatasets(category, apps, bucketer);
			datasets.forEach(results::put);
			response = ResponseEntity.ok(results);
		} catch (IllegalArgumentException e) {
			response = ResponseEntity.badRequest()
					.body(Collections.singletonMap("error", Collections.singletonList(e.getMessage())));
		}
		return response;
	}

	private Map<String, List<Number>> getCategoryDatasets(String category, List<VeracodeDastAppModel> apps,
			SimpleBucketer<VeracodeDastReportModel> bucketer) {
		switch (category) {
		case "policy":
			return getPolicyScoreDatasets(apps, bucketer);
		case "flaws":
			return getFlawDatasets(apps, bucketer);
		case "severity":
			return getSeverityDatasets(apps, bucketer);
		default:
			throw new IllegalArgumentException("Unknown categorization");
		}
	}

	private Map<String, List<Number>> getPolicyScoreDatasets(List<VeracodeDastAppModel> apps,
			SimpleBucketer<VeracodeDastReportModel> bucketer) {
		Map<String, List<LongSummaryStatistics>> datasets = new LinkedHashMap<>();

		for (VeracodeDastAppModel app : apps) {
			List<List<VeracodeDastReportModel>> reports = bucketer.putItemsInBuckets(app.getReports());
			for (int i = 0; i < reports.size(); i++) {
				if (reports.get(i).isEmpty()) {
					continue;
				}
				for (VeracodeDastReportModel report : reports.get(i)) {
					updateAverages(datasets, i, reports.size(), "Policy Score", report.getScore());
				}
			}
		}
		Map<String, List<Number>> policySummary = new LinkedHashMap<>();
		for (Entry<String, List<LongSummaryStatistics>> entry : datasets.entrySet()) {
			policySummary.put(entry.getKey(),
					entry.getValue().stream().map(lss -> lss.getAverage()).collect(Collectors.toList()));
		}
		return policySummary;
	}

	private Map<String, List<Number>> getFlawDatasets(List<VeracodeDastAppModel> apps,
			SimpleBucketer<VeracodeDastReportModel> bucketer) {
		Map<String, List<Number>> datasets = new LinkedHashMap<>();

		for (VeracodeDastAppModel app : apps) {
			List<List<VeracodeDastReportModel>> reports = bucketer.putItemsInBuckets(app.getReports());
			for (int i = 0; i < reports.size(); i++) {
				if (reports.get(i).isEmpty()) {
					continue;
				}
				for (VeracodeDastReportModel report : reports.get(i)) {
					updateCounts(datasets, i, reports.size(), "Total Flaws", report.getTotalFlaws());
					updateCounts(datasets, i, reports.size(), "Unmitigated Flaws", report.getUnmitigatedFlaws());
				}
			}
		}
		return datasets;
	}
	

	private Map<String, List<Number>> getSeverityDatasets(List<VeracodeDastAppModel> apps,
			SimpleBucketer<VeracodeDastReportModel> bucketer) {
		Map<String, List<Number>> datasets = new LinkedHashMap<>();

		for (VeracodeDastAppModel app : apps) {
			List<List<VeracodeDastReportModel>> reports = bucketer.putItemsInBuckets(app.getReports());
			for (int i = 0; i < reports.size(); i++) {
				if (reports.get(i).isEmpty()) {
					continue;
				}
				for (VeracodeDastReportModel report : reports.get(i)) {
					updateCounts(datasets, i, reports.size(), "Very High", report.getvHigh());
					updateCounts(datasets, i, reports.size(), "High", report.getHigh());
					updateCounts(datasets, i, reports.size(), "Medium", report.getMedium());
					updateCounts(datasets, i, reports.size(), "Low", report.getLow());
					updateCounts(datasets, i, reports.size(), "Very Low", report.getvLow());
					updateCounts(datasets, i, reports.size(), "Informational", report.getInfo());
				}
			}
		}
		return datasets;
	}


	private void updateCounts(Map<String, List<Number>> datasets, int index, int listSize, String key, long value) {
		if (datasets.containsKey(key)) {
			List<Number> counts = datasets.get(key);
			counts.set(index, counts.get(index).longValue() + value);
		} else {
			List<Number> counts = new ArrayList<>();
			IntStream.range(0, listSize).forEach(idx -> counts.add(0L));
			counts.set(index, value);
			datasets.put(key, counts);
		}
	}

	private void updateAverages(Map<String, List<LongSummaryStatistics>> datasets, int index, int listSize, String key,
			long value) {
		if (datasets.containsKey(key)) {
			List<LongSummaryStatistics> counts = datasets.get(key);
			counts.get(index).accept(value);
		} else {
			List<LongSummaryStatistics> counts = new ArrayList<>();
			IntStream.range(0, listSize).forEach(idx -> counts.add(new LongSummaryStatistics()));
			counts.get(index).accept(value);
			datasets.put(key, counts);
		}
	}

	private Supplier<LocalDateTime> getOldestReportDate(List<VeracodeDastAppModel> apps) {
		return () -> {
			Optional<LocalDateTime> localOpt = apps.stream().filter(app -> app.getOldestReport() != null)
					.map(app -> app.getOldestReport().getReportDate()).min(Comparator.naturalOrder());
			return localOpt.orElseGet(LocalDateTime::now);
		};
	}

}
