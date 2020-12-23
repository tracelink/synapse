package com.tracelink.prodsec.plugin.jira.model;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;

import javax.persistence.Convert;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;

/**
 * The Database entity for the Jira Search Phrases.
 *
 * @author bhoran
 */
@Entity
@Table(schema = JiraPlugin.SCHEMA, name = "jira_search_phrases")
public class JiraPhrases {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "query_id")
	private long id;

	@Column(name = "jql_phrase")
	private String jql;

	@Column(name = "data_format")
	@Convert(converter = JiraPhraseDataFormat.JiraPhraseDataFormatConverter.class)
	private JiraPhraseDataFormat dataFormat;

	public long getId() {
		return id;
	}

	public String getJQL() {
		return jql;
	}

	public void setJQL(String jql) {
		this.jql = jql;
	}

	public JiraPhraseDataFormat getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(JiraPhraseDataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

}
