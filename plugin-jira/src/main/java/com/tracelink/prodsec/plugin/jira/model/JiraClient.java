package com.tracelink.prodsec.plugin.jira.model;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URL;

/**
 * The Database entity for the Jira API client.
 *
 * @author bhoran
 */
@Entity
@Table(schema = JiraPlugin.SCHEMA, name = "jira_clients")
public class JiraClient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_id")
	private long id;

	@Column(name = "api_url")
	private URL apiUrl;

	@Column(name = "username")
	private String user;

	@Column(name = "authentication")
	private String auth;

	public long getId() {
		return id;
	}

	public URL getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(URL apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}
}
