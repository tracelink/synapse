package com.tracelink.prodsec.synapse.spi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;

/**
 * Entity description for a plugin. Can be activated/deactivated
 * 
 * @author csmith
 *
 */
@Entity
@Table(name = "plugins", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
public class PluginModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "plugin_id")
	private long id;

	@Column(name = "plugin_name")
	private String pluginName;

	@Column(name = "activated")
	private boolean activated;

	public long getId() {
		return id;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

}
