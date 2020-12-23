package com.tracelink.prodsec.plugin.veracode.sca.model;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Database entity for the Veracode SCA workspace.
 *
 * @author mcool
 */
@Entity
@Table(schema = VeracodeScaPlugin.SCHEMA, name = "veracode_sca_workspaces")
public class VeracodeScaWorkspace {

	/**
	 * The ID of this project, which is a UUID.
	 */
	@Id
	@Column(name = "workspace_id")
	private UUID id;

	/**
	 * The name of this project.
	 */
	@Column(name = "name")
	private String name;

	/**
	 * The site ID of this project.
	 */
	@Column(name = "site_id")
	private String siteId;

	/**
	 * Whether the projects and issues associated with this workspace should be excluded by
	 * Synapse. If excluded, the projects and issues will not be displayed in graphs, summary
	 * statistics, or the issues page.
	 */
	@Column(name = "included")
	private boolean included = true;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}
}
