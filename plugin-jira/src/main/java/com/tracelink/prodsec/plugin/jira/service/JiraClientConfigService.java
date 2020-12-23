package com.tracelink.prodsec.plugin.jira.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.tracelink.prodsec.plugin.jira.exception.JiraClientException;
import com.tracelink.prodsec.plugin.jira.model.JiraClient;
import com.tracelink.prodsec.plugin.jira.repo.JiraClientRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to store and retrieve data about the Jira API client from the
 * {@link JiraClientRepository}.
 */
@Service
public class JiraClientConfigService {

	private final JiraClientRepository clientRepository;

	/**
	 * Creates this client service with a pre-configured repository: the
	 * {@link JiraClientRepository} to store the Jira client's information.
	 *
	 * @param clientRepository the Jira API client repository
	 */
	public JiraClientConfigService(@Autowired JiraClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	/**
	 * Gets the configured Jira API client from the database.
	 *
	 * @return the configured API client
	 * @throws JiraClientException if no API client is configured
	 */
	public JiraClient getClient() throws JiraClientException {
		List<JiraClient> clients = clientRepository.findAll();
		if (clients.isEmpty()) {
			throw new JiraClientException("No Jira client configured.");
		}
		return clients.get(0);
	}

	/**
	 * Sets the values of the Jira API client in the database. If no API
	 * client is currently configured, it will create a new entity. Otherwise,
	 * it will update the existing entity. Only sets the values if the given URL
	 * is properly formed.
	 *
	 * @param apiUrl URL for the Jira server API
	 * @param user   username for the Jira server
	 * @param auth   authentication for the Jira server
	 * @return the saved JiraClient config
	 */
	public JiraClient setClient(URL apiUrl, String user, String auth) {
		List<JiraClient> clients = clientRepository.findAll();
		JiraClient client;
		if (clients.isEmpty()) {
			client = new JiraClient();
		} else {
			client = clients.get(0);
		}
		client.setApiUrl(apiUrl);
		client.setUser(user);
		client.setAuth(auth);
		return clientRepository.saveAndFlush(client);
	}

	/**
	 * Using the client configured in the database, creates a {@link JiraRestClient}
	 *
	 * @return a rest client configured with authentication to communicate with Jira
	 * @throws URISyntaxException  If the client URI could not be parsed
	 * @throws JiraClientException If there is no client configured
	 */
	public JiraRestClient createRestClient() throws URISyntaxException, JiraClientException {
		JiraClient client = getClient();
		URI uri = client.getApiUrl().toURI();
		AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

		return factory.createWithBasicHttpAuthentication(uri, client.getUser(), client.getAuth());
	}
}
