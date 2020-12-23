package com.tracelink.prodsec.plugin.jira.model;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * The Database entity for vulnerable issue metrics.
 *
 * @author bhoran
 */

@Entity
@Table(schema = JiraPlugin.SCHEMA, name = "jira_vuln")
public class JiraVuln {

	@Id
	@Column(name = "issue_id")
	private long id;

	@Column(name = "issue_key")
	private String issueKey;

	@Column(name = "sev")
	private String sev;

	@Column(name = "created")
	private LocalDate created;

	@Column(name = "resolved")
	private LocalDate resolved;

	@Transient
	@Column(name = "sla_status")
	private String slaStatus;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "productline_id")
	private ProductLineModel productLine;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKey() {
		return issueKey;
	}

	public void setKey(String key) {
		this.issueKey = key;
	}

	public String getSev() {
		return sev;
	}

	public void setSev(String sev) {
		this.sev = sev;
	}

	public LocalDate getCreated() {
		return created;
	}

	public void setCreated(LocalDate created) {
		this.created = created;
	}

	public LocalDate getResolved() {
		return resolved;
	}

	public void setResolved(LocalDate resolved) {
		this.resolved = resolved;
	}

	public String getSlaStatus() {
		return slaStatus;
	}

	public void setSlaStatus(String slaStatus) {
		this.slaStatus = slaStatus;
	}

	public ProductLineModel getProductLine() {
		return productLine;
	}

	public void setProductLine(ProductLineModel productLine) {
		this.productLine = productLine;
	}
}
