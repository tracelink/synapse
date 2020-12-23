package com.tracelink.prodsec.plugin.jira.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.tracelink.prodsec.plugin.jira.exception.JiraClientException;
import com.tracelink.prodsec.plugin.jira.model.JiraClient;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Handles business logic for updating the scrum metrics and vulnerability metrics,
 * by initiating getting the data from the Jira server. Also contains logic to test the connection
 * to the server. Updater takes a "snapshot" and does not  delete old metrics, even if they
 * have been resolved.
 *
 * @author bhoran
 */
@Service
public class JiraUpdateService {

	private static final Logger LOG = LoggerFactory.getLogger(JiraUpdateService.class);

	/* The test phrase constant can be any JQL phrase meant to test a connection can
	 *  be made to search for issues */
	private static final String TEST_PHRASE = "type = Bug";

	private final JiraScrumMetricsService appSecMetricsService;

	private final JiraVulnMetricsService vulnMetricsService;

	private final JiraClientConfigService configService;

	public JiraUpdateService(@Autowired JiraScrumMetricsService appSecMetricsService,
			@Autowired JiraVulnMetricsService vulnMetricsService,
			@Autowired JiraClientConfigService configService) {
		this.appSecMetricsService = appSecMetricsService;
		this.vulnMetricsService = vulnMetricsService;
		this.configService = configService;
	}

	/**
	 * Determines whether a connection can be established with the Jira
	 * server, using the configured API client settings.
	 *
	 * @return true if data can be fetched
	 * false if no API client is configured, causing a {@link RestClientException}
	 * or if additional exceptions are thrown in the search process
	 */
	public boolean testConnection() {
		try {
			JiraRestClient restClient = configService.createRestClient();
			int numIssues = restClient.getSearchClient().searchJql(TEST_PHRASE).get().getTotal();
		} catch (RestClientException | URISyntaxException | JiraClientException
				| InterruptedException | ExecutionException e) {
			LOG.error(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Fetches new data from Jira, initiates services that add it to the database
	 */
	public void syncAllData() {
		LOG.info("Beginning Jira data update");
		try {
			// get the current client
			JiraClient client = configService.getClient();
			if (client == null) {
				LOG.error("No Configuration for Jira client");
				return;
			}
			// start the sync operation
			dataSync();

		} catch (Exception e) {
			LOG.error("Jira data update failed due to error: " + e.getMessage());
		}
		LOG.info("Jira data update complete");
		logVulns();
	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Save the scrum metrics</li>
	 * <li>Save the vulnerability metrics</li>
	 * </ol>
	 */
	private void dataSync() {
		try {
			appSecMetricsService.storeScrumMetrics();
			vulnMetricsService.storeVulnMetrics();
		} catch (Exception e) {
			LOG.error("Error syncing data due to exception: " + e.getMessage());
		}
	}

	private void logVulns() {
		long numVulns = vulnMetricsService.getAllUnresolvedMetrics().size();
		LOG.info("Found " + numVulns + " vulnerabilities");
	}
}
