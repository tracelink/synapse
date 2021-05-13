package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueType;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaIssueService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.util.bucketer.AbstractBucketer;
import com.tracelink.prodsec.synapse.util.bucketer.StandardIntervalBucketer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Veracode SCA rest controller handles REST requests for retrieving Veracode SCA issues data to
 * be displayed in the graph on the Veracode SCA dashboard page.
 *
 * @author mcool
 */
@RestController
@RequestMapping("/veracode/sca/rest")
public class VeracodeScaRestController {

	private final VeracodeScaProjectService projectService;
	private final VeracodeScaIssueService issueService;

	public VeracodeScaRestController(@Autowired VeracodeScaProjectService projectService,
			@Autowired VeracodeScaIssueService issueService) {
		this.projectService = projectService;
		this.issueService = issueService;
	}

	/**
	 * Gets issues for all projects over time, regardless of whether the project is mapped.
	 *
	 * @param period   the time period over which issues should be gathered
	 * @param category the category to separate issues by
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/issues", params = {"period", "category"})
	public ResponseEntity<Map<String, List<?>>> getIssues(@RequestParam String period,
			@RequestParam String category) {
		// Get all Veracode SCA projects
		List<VeracodeScaProject> projects = projectService.getIncludedProjects();
		return getIssuesHelper(projects, period, category);
	}

	/**
	 * Gets issues for a Synapse product line over time, separated by project.
	 *
	 * @param productLine the product line to gather issues for
	 * @param period      the time period over which issues should be gathered
	 * @param category    the category to separate issues by
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/issues", params = {"productLine", "period", "category"})
	public ResponseEntity<Map<String, List<?>>> getIssuesForProductLine(
			@RequestParam String productLine, @RequestParam String period,
			@RequestParam String category) {
		// Get Veracode SCA projects mapped to the given product line
		List<VeracodeScaProject> mappedProjects = projectService.getMappedProjects().stream()
				.filter(a -> a.getSynapseProject().getOwningProductLine().getName()
						.equals(productLine))
				.collect(Collectors.toList());

		return getIssuesHelper(mappedProjects, period, category);
	}

	/**
	 * Gets issues for a Synapse project filter over time, separated by project.
	 *
	 * @param projectFilter the project filter to gather issues for
	 * @param period        the time period over which issues should be gathered
	 * @param category      the category to separate issues by
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/issues", params = {"projectFilter", "period", "category"})
	public ResponseEntity<Map<String, List<?>>> getIssuesForProjectFilter(
			@RequestParam String projectFilter, @RequestParam String period,
			@RequestParam String category) {
		// Get Veracode SCA projects mapped to the given project filter
		List<VeracodeScaProject> mappedProjects = projectService
				.getMappedProjects().stream()
				.filter(a -> a.getSynapseProject().getFilters().stream()
						.map(ProjectFilterModel::getName).collect(Collectors.toList())
						.contains(projectFilter))
				.collect(Collectors.toList());

		return getIssuesHelper(mappedProjects, period, category);
	}

	/**
	 * Gets issues for a single Synapse project over time.
	 *
	 * @param project  the project to gather issues for
	 * @param period   the time period over which issues should be gathered
	 * @param category the category to separate issues by
	 * @return map containing graph data and labels
	 */
	@GetMapping(value = "/issues", params = {"project", "period", "category"})
	public ResponseEntity<Map<String, List<?>>> getIssuesForProject(
			@RequestParam String project, @RequestParam String period,
			@RequestParam String category) {
		// Get Veracode SCA projects mapped to the given project
		List<VeracodeScaProject> mappedProjects = projectService.getMappedProjects().stream()
				.filter(a -> a.getSynapseProject().getName().equals(project))
				.collect(Collectors.toList());

		return getIssuesHelper(mappedProjects, period, category);
	}

	private ResponseEntity<Map<String, List<?>>> getIssuesHelper(
			List<VeracodeScaProject> projects, String period, String category) {
		AbstractBucketer<VeracodeScaIssue> bucketer;
		try {
			bucketer = new VeracodeScaIssueBucketer(period, issueService::getEarliestIssueDate);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(Collections
							.singletonMap("error", Collections.singletonList(e.getMessage())));
		}

		Map<String, List<?>> results = new LinkedHashMap<>();
		results.put("labels", bucketer.getBucketIntervals().getLabels());
		Map<String, List<Long>> datasets;
		try {
			datasets = getDatasets(category, projects, bucketer);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(Collections
							.singletonMap("error", Collections.singletonList(e.getMessage())));
		}
		datasets.forEach(results::put);
		return ResponseEntity.ok(results);
	}

	private Map<String, List<Long>> getDatasets(String category, List<VeracodeScaProject> projects,
			AbstractBucketer<VeracodeScaIssue> bucketer) {

		// Get all issues for the projects and place them in buckets
		List<List<VeracodeScaIssue>> issues = bucketer.putItemsInBuckets(
				projects.stream().map(VeracodeScaProject::getIssuesForVisibleBranch)
						.flatMap(List::stream).collect(Collectors.toList()));

		// Get datasets according to category
		switch (category) {
			case "severity":
				return getSeverityDatasets(issues);
			case "vulnerability":
				return getVulnerabilityDatasets(issues);
			case "type":
				return getIssueTypeDatasets(issues);
			default:
				throw new IllegalArgumentException("Unknown categorization.");
		}
	}

	private Map<String, List<Long>> getSeverityDatasets(List<List<VeracodeScaIssue>> issues) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		// Create severity counts lists
		List<Long> highIssues = new ArrayList<>();
		List<Long> mediumIssues = new ArrayList<>();
		List<Long> lowIssues = new ArrayList<>();
		List<Long> highVmIssues = new ArrayList<>();
		List<Long> mediumVmIssues = new ArrayList<>();
		List<Long> lowVmIssues = new ArrayList<>();

		for (List<VeracodeScaIssue> veracodeScaIssues : issues) {
			// Set count of high issues
			highIssues.add(veracodeScaIssues.stream().filter(issue ->
					issue.getSeverityString().equals(VeracodeScaIssue.SEVERITY_HIGH)
							&& !issue.isVulnerableMethod()).count());
			// Set count of medium issues
			mediumIssues.add(veracodeScaIssues.stream().filter(issue ->
					issue.getSeverityString().equals(VeracodeScaIssue.SEVERITY_MEDIUM)
							&& !issue.isVulnerableMethod()).count());
			// Set count of low issues
			lowIssues.add(veracodeScaIssues.stream().filter(issue ->
					issue.getSeverityString().equals(VeracodeScaIssue.SEVERITY_LOW)
							&& !issue.isVulnerableMethod()).count());
			// Set count of high vulnerable methods issues
			highVmIssues.add(veracodeScaIssues.stream().filter(issue ->
					issue.getSeverityString().equals(VeracodeScaIssue.SEVERITY_HIGH)
							&& issue.isVulnerableMethod()).count());
			// Set count of medium vulnerable methods issues
			mediumVmIssues.add(veracodeScaIssues.stream().filter(issue ->
					issue.getSeverityString().equals(VeracodeScaIssue.SEVERITY_MEDIUM)
							&& issue.isVulnerableMethod()).count());
			// Set count of low vulnerable methods issues
			lowVmIssues.add(veracodeScaIssues.stream().filter(issue ->
					issue.getSeverityString().equals(VeracodeScaIssue.SEVERITY_LOW)
							&& issue.isVulnerableMethod()).count());
		}

		// Add severity counts lists to the datasets
		datasets.put(VeracodeScaIssue.SEVERITY_HIGH, highIssues);
		datasets.put(VeracodeScaIssue.SEVERITY_MEDIUM, mediumIssues);
		datasets.put(VeracodeScaIssue.SEVERITY_LOW, lowIssues);
		String withVm = " with VM";
		datasets.put(VeracodeScaIssue.SEVERITY_HIGH + withVm, highVmIssues);
		datasets.put(VeracodeScaIssue.SEVERITY_MEDIUM + withVm, mediumVmIssues);
		datasets.put(VeracodeScaIssue.SEVERITY_LOW + withVm, lowVmIssues);
		return datasets;
	}

	private Map<String, List<Long>> getVulnerabilityDatasets(List<List<VeracodeScaIssue>> issues) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		for (int i = 0; i < issues.size(); i++) {
			for (VeracodeScaIssue issue : issues.get(i)) {
				if (!issue.getIssueType().equals(IssueType.VULNERABILITY)) {
					continue;
				}
				String vuln = issue.getVulnerability();
				if (StringUtils.isBlank(vuln)) {
					vuln = "Not Specified";
				}
				if (datasets.containsKey(vuln)) {
					List<Long> counts = datasets.get(vuln);
					counts.set(i, counts.get(i) + 1);
				} else {
					List<Long> counts = new ArrayList<>();
					IntStream.range(0, issues.size()).forEach(index -> counts.add(0L));
					counts.set(i, 1L);
					datasets.put(vuln, counts);
				}
			}
		}
		return datasets;
	}

	private Map<String, List<Long>> getIssueTypeDatasets(List<List<VeracodeScaIssue>> issues) {
		Map<String, List<Long>> datasets = new LinkedHashMap<>();

		// Create issue type counts lists
		List<Long> vulnerabilityIssues = new ArrayList<>();
		List<Long> licenseIssues = new ArrayList<>();
		List<Long> libraryIssues = new ArrayList<>();
		// Populate with enough zeros for the number of buckets
		IntStream.range(0, issues.size()).forEach(index -> {
			vulnerabilityIssues.add(0L);
			licenseIssues.add(0L);
			libraryIssues.add(0L);
		});

		for (int i = 0; i < issues.size(); i++) {
			// Set count of vulnerability issues
			vulnerabilityIssues.set(i, issues.get(i).stream()
					.filter(issue -> issue.getIssueType().equals(IssueType.VULNERABILITY)).count());
			// Set count of license issues
			licenseIssues.set(i, issues.get(i).stream()
					.filter(issue -> issue.getIssueType().equals(IssueType.LICENSE)).count());
			// Set count of library issues
			libraryIssues.set(i, issues.get(i).stream()
					.filter(issue -> issue.getIssueType().equals(IssueType.LIBRARY)).count());
		}

		// Add severity counts lists to the datasets
		datasets.put(IssueType.VULNERABILITY.getValue(), vulnerabilityIssues);
		datasets.put(IssueType.LICENSE.getValue(), licenseIssues);
		datasets.put(IssueType.LIBRARY.getValue(), libraryIssues);
		return datasets;
	}

	private static class VeracodeScaIssueBucketer extends
			StandardIntervalBucketer<VeracodeScaIssue> {

		VeracodeScaIssueBucketer(String period, Supplier<LocalDateTime> earliestDateTimeSupplier) {
			super(period, earliestDateTimeSupplier);
		}

		@Override
		public boolean itemBelongsInBucket(VeracodeScaIssue item, LocalDateTime startDateTime,
				LocalDateTime endDateTime) {

			// If the issue was created after the period ended, then it does not belong in the
			// bucket
			if (item.getCreatedDate().compareTo(endDateTime) >= 0) {
				return false;
			}
			// If the issue was last updated before the period started, then it does not belong in
			// the bucket
			if (item.getLastUpdatedDate().compareTo(startDateTime) < 0) {
				return false;
			}
			// If the issue is fixed and the fixed date is before the period ended, then it does not
			// belong in the bucket
			if (item.getIssueStatus().equals(IssueStatus.FIXED) && item.getFixedDate() != null) {
				if (item.getFixedDate().compareTo(endDateTime) < 0) {
					return false;
				}
			}
			// If the issue is ignored and the ignored date is before the period ended, then it does
			// not belong in the bucket
			if (item.isIgnored() && item.getIgnoredDate() != null) {
				return item.getIgnoredDate().compareTo(endDateTime) >= 0;
			}
			// In all other cases, the issue is active and belongs in the bucket
			return true;
		}
	}
}
