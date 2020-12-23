package com.tracelink.prodsec.plugin.jira.service;

import com.tracelink.prodsec.plugin.jira.exception.JiraPhrasesException;
import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraPhrases;
import com.tracelink.prodsec.plugin.jira.repo.JiraSearchPhrasesRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles business logic to store and retrieve search phrase data from the
 * {@link JiraSearchPhrasesRepo}. Search Phrases correspond with a keyword,
 * or DataFormat, within the service, indicating that the search phrase used
 * should correspond with the abilities of that service to parse the information
 * pulled.
 */
@Service
public class JiraPhrasesService {

	private final JiraSearchPhrasesRepo jiraSearchPhrasesRepo;

	public JiraPhrasesService(@Autowired JiraSearchPhrasesRepo jiraSearchPhrasesRepo) {
		this.jiraSearchPhrasesRepo = jiraSearchPhrasesRepo;
	}

	/**
	 * Gets the configured JQL Phrases from the database.
	 *
	 * @return List of the configured JQL Phrases
	 * @throws JiraPhrasesException if no search phrases are configured
	 */
	public List<JiraPhrases> getPhrases() throws JiraPhrasesException {
		List<JiraPhrases> phrases = jiraSearchPhrasesRepo.findAll();
		if (phrases.isEmpty()) {
			throw new JiraPhrasesException("No Jira phrases configured.");
		}
		return phrases;
	}

	/**
	 * Sets the values of dataFormat and the configured JQL Phrase in the database.
	 *
	 * @param dataFormat JiraPhraseDataFormat is associated with a service/plug-in page to
	 *                   specify what information it should pull from the Jira Server
	 * @param jqlString  the new Jira Query Language phrase to query for the expected data
	 * @return the {@link JiraPhrases} object representing the change in JQL for the
	 * the specified data format
	 */
	public JiraPhrases setPhraseForDataFormat(String jqlString, JiraPhraseDataFormat dataFormat) {
		List<JiraPhrases> phrases = jiraSearchPhrasesRepo.findByDataFormat(dataFormat);
		JiraPhrases phrase;
		if (phrases.isEmpty()) {
			phrase = new JiraPhrases();
		} else {
			phrase = phrases.get(0);
		}
		phrase.setDataFormat(dataFormat);
		phrase.setJQL(jqlString);
		return jiraSearchPhrasesRepo.saveAndFlush(phrase);
	}

	/**
	 * Given an enumerated type JiraPhraseDataFormat dataFormat, representing the service this
	 * phrase will correspond to when getting data from the Jira Server, getPhraseForData() returns
	 * the configured JQL phrase from the database for the dataFormat
	 *
	 * @param dataFormat JiraPhraseDataFormat associated with a service/plug-in page that specifies
	 *                   what information it should pull from the Jira Server
	 * @return The configured JQL phrase as a string
	 * @throws JiraPhrasesException if no search phrases are configured for a services dataFormat
	 */
	public String getPhraseForData(JiraPhraseDataFormat dataFormat) throws JiraPhrasesException {
		JiraPhrases phrase = jiraSearchPhrasesRepo.findByDataFormat(dataFormat).get(0);
		if (phrase == null) {
			throw new JiraPhrasesException(
					"No phrase configured for " + dataFormat.getDisplayName() + " in the database");
		}
		return phrase.getJQL();
	}
}
