package com.tracelink.prodsec.plugin.jira.model;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Database entity for the time an issue is allowed to remain before a vulnerability
 * must be fixed as per a companies Service Level Agreement (SLA) for each severity.
 * These AllowedSla values are configurable so allowed time for a level of issue
 * (critical - informational) can be adjusted, if needed, over time.
 *
 * @author bhoran
 */
@Entity
@Table(schema = JiraPlugin.SCHEMA, name = "jira_allowed_sla")
public class JiraAllowedSla {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sla_id")
	private long id;

	@Column(name = "severity")
	private String severity;

	@Column(name = "allowed_days")
	private Integer allowedDays;

	public long getId() {
		return id;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public Integer getAllowedDays() {
		return allowedDays;
	}

	public void setAllowedDays(Integer allowedDays) {
		this.allowedDays = allowedDays;
	}
}
