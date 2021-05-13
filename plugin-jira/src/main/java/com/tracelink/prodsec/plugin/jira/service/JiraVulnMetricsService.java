package com.tracelink.prodsec.plugin.jira.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.tracelink.prodsec.plugin.jira.exception.JiraMappingsException;
import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.repo.JiraVulnMetricsRepo;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for storing and retrieving vulnerabilities in the
 * {@link JiraVulnMetricsRepo}. Provides functionality to edit vulnerabilities,
 * by connecting or disconnecting them from a product line
 *
 * @author bhoran
 */
@Service
public class JiraVulnMetricsService {

	// Assigns a severity to issues where severity was not assigned by the vuln reporter
	private static final String UNKNOWN_SEVERITY = "Unknown";

	private final JiraClientConfigService clientService;
	private final JiraVulnMetricsRepo jiraVulnMetricsRepo;
	private final JiraAllowedSlaService allowedDaysService;
	private final JiraPhrasesService jiraPhraseService;

	/**
	 * Creates this SearchJqlBugsService with the pre-configured database
	 * related to the desired metrics for each search
	 * {@link JiraVulnMetricsRepo} to interact with the database, and add
	 * {@link JiraVuln} instances
	 *
	 * @param jiraVulnMetricsRepo the Jira Vulnerabilities repository
	 * @param clientService       the configuration service for the Jira Client
	 * @param allowedDaysService  the configuration service for days allowed in SLA
	 * @param jiraPhraseService   the configuration service for the JQL Search Phrases
	 */
	public JiraVulnMetricsService(@Autowired JiraVulnMetricsRepo jiraVulnMetricsRepo,
			@Autowired JiraClientConfigService clientService,
			@Autowired JiraAllowedSlaService allowedDaysService,
			@Autowired JiraPhrasesService jiraPhraseService) {
		this.jiraVulnMetricsRepo = jiraVulnMetricsRepo;
		this.clientService = clientService;
		this.allowedDaysService = allowedDaysService;
		this.jiraPhraseService = jiraPhraseService;
	}

	/**
	 * Gets vulnerability metrics from Jira and stores them in the database.
	 *
	 * @throws Exception if there ar problems with the Jira REST client
	 */
	public void storeVulnMetrics() throws Exception {
		String vulnsJqlSearch = jiraPhraseService.getPhraseForData(JiraPhraseDataFormat.VULN);
		JiraRestClient restClient = clientService.createRestClient();
		List<Issue> totalIssues = new ArrayList<>();
		SearchResult searchResult;
		int start = 0;
		int pageSize = 100;

		/* Iterate through all issues by getting a certain page size of issues, and repeating with
		 * the next "page" of issues until all are seen */
		do {
			searchResult = restClient.getSearchClient()
					.searchJql(vulnsJqlSearch, pageSize, start, null).get();
			searchResult.getIssues().forEach(totalIssues::add);
			start += searchResult.getMaxResults();
		} while (start < searchResult.getTotal());

		totalIssues.forEach(i -> {
			long id = i.getId();
			String key = i.getKey();

			/*
			 * If getting the Value of the Security Severity returns null, this marks the severity
			 *  as an Unknown severity assignment to avoid a Null Pointer Exception.
			 */
			IssueField securitySeverity = i.getFieldByName("Security Severity");
			String sev;
			if (securitySeverity == null || securitySeverity.getValue() == null) {
				sev = UNKNOWN_SEVERITY;
			} else {
				sev = parseJSONFor(securitySeverity.getValue().toString(), "value");
			}

			LocalDate create = dateConvert(i.getCreationDate().toString());
			IssueField resField = i.getFieldByName("Resolved");
			LocalDate resolved = resField == null || resField.getValue() == null ? null
					: dateConvert(resField.getValue().toString());

			JiraVuln jiraVulnMetricsEntity = jiraVulnMetricsRepo.findById(id)
					.orElse(new JiraVuln());
			jiraVulnMetricsEntity.setId(id);
			jiraVulnMetricsEntity.setKey(key);
			jiraVulnMetricsEntity.setSev(sev);
			jiraVulnMetricsEntity.setCreated(create);
			jiraVulnMetricsEntity.setResolved(resolved);
			jiraVulnMetricsEntity.setProductLine(null);
			jiraVulnMetricsRepo.save(jiraVulnMetricsEntity);
		});
		jiraVulnMetricsRepo.flush();
	}

	public List<JiraVuln> getAllVulnMetrics() {
		return jiraVulnMetricsRepo.findAll();
	}

	public JiraVuln getOldestMetrics() {
		return jiraVulnMetricsRepo.findTopByOrderByCreatedAsc();
	}

	public List<JiraVuln> getAllUnresolvedMetrics() {
		List<JiraVuln> unresolvedVulnsWithSla = new ArrayList<>();
		List<JiraVuln> unresolvedMetrics = jiraVulnMetricsRepo.findAllByResolvedIsNull();

		if (unresolvedMetrics != null) {
			for (JiraVuln v : unresolvedMetrics) {
				String slaStatus = calculateSlaStatusString(v);
				v.setSlaStatus(slaStatus);

				unresolvedVulnsWithSla.add(v);
			}
		}
		return unresolvedVulnsWithSla;
	}

	public List<JiraVuln> getUnresolvedVulnsForProductLine(ProductLineModel productLine) {
		List<JiraVuln> vulnerabilities = jiraVulnMetricsRepo.findAllByProductLine(productLine);
		List<JiraVuln> unresolvedVulns = new ArrayList<>();

		if (vulnerabilities != null) {
			for (JiraVuln v : vulnerabilities) {
				if (v.getResolved() == null) {
					unresolvedVulns.add(v);
				}
			}
		}
		return unresolvedVulns;
	}

	/**
	 * Creates a mapping between a Synapse product line and a Jira vulnerability to correctly
	 * display metrics.
	 *
	 * @param synapseProduct the {@link ProductLineModel} to map the vulnerability to
	 * @param id             the ID of the Jira vulnerability
	 * @throws JiraMappingsException if there is no such Jira vulnerability
	 */
	public void createMapping(ProductLineModel synapseProduct, long id)
			throws JiraMappingsException {
		Optional<JiraVuln> vulnEntity = jiraVulnMetricsRepo.findById(id);
		if (vulnEntity.isPresent()) {
			JiraVuln vuln = vulnEntity.get();
			vuln.setProductLine(synapseProduct);
			jiraVulnMetricsRepo.saveAndFlush(vuln);
		} else {
			throw new JiraMappingsException(
					"Error creating mapping, vulnerability selected was not found in database");
		}
	}

	/**
	 * Deletes a mapping between a Synapse product line and a Jira vulnerability.
	 *
	 * @param id ID of the Jira vulnerability to unmap
	 * @throws JiraMappingsException if there is no such Jira vulnerability
	 */
	public void deleteMapping(long id) throws JiraMappingsException {
		Optional<JiraVuln> vulnEntity = jiraVulnMetricsRepo.findById(id);
		if (vulnEntity.isPresent()) {
			JiraVuln vuln = vulnEntity.get();
			vuln.setProductLine(null);
			jiraVulnMetricsRepo.saveAndFlush(vuln);
		} else {
			throw new JiraMappingsException(
					"Error deleting mapping, vulnerability selected was not found in database");
		}
	}

	private String calculateSlaStatusString(JiraVuln vuln) {
		Integer allowedTime = allowedDaysService.getAllowedTimeBySev(vuln.getSev());

		if (allowedTime == null) {
			return "N/A";
		}

		LocalDate currentDate = LocalDate.now();
		LocalDate endOfSla = vuln.getCreated().plusDays(allowedTime);

		if (endOfSla.compareTo(currentDate) >= 0) {
			return "In SLA";
		} else if (endOfSla.compareTo(currentDate) < 0) {
			return "" + ChronoUnit.DAYS.between(endOfSla, currentDate) + " days past SLA";
		} else {
			return "Error Parsing";
		}
	}

	private String parseJSONFor(String json, String key) {
		try {
			return new JSONObject(json).get(key).toString();
		} catch (Exception e) {
			return "Parse Error";
		}
	}

	private LocalDate dateConvert(String orig) {
		ZonedDateTime convert;
		try {
			convert = ZonedDateTime.parse(orig);
		} catch (DateTimeParseException e) {
			String corrected = new StringBuilder(orig).insert(orig.length() - 2, ":").toString();
			convert = ZonedDateTime.parse(corrected);
		}
		return convert.toLocalDate();
	}
}
