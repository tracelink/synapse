package com.tracelink.prodsec.plugin.demo.model;

import com.tracelink.prodsec.plugin.demo.DemoPlugin;
import com.tracelink.prodsec.synapse.encryption.converter.AbstractEncryptedAttributeConverter;
import com.tracelink.prodsec.synapse.encryption.converter.StringEncryptedAttributeConverter;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The Database entity for the Demo.
 * <p>
 * Demonstrates how to relate this object to a Synapse Core
 * {@link ProjectModel}.
 * <p>
 * Note that the {@link Table} annotation is marked with the schema name from
 * the plugin
 *
 * @author csmith
 */
@Entity
@Table(schema = DemoPlugin.SCHEMA, name = "demoproject")
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DemoProjectEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "demo_id")
	private long id;

	@Column(name = "vuln")
	private int vuln;

	/*
	 * I believe implementers will always want to eagerly fetch the joined object.
	 * Without this, or marked as LAZY, the DB link will not be held, and you will
	 * receive an exception.
	 *
	 * Use a Join Column (or Join Table for Many to Many) to link with a Synapse
	 * Core DB column.
	 *
	 * The other half of this setup happens in the
	 * /src/main/resources/db/demo/V001*.sql file
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synapse_project")
	private ProjectModel synapseProject;

	/**
	 * Adding the {@link @Convert} annotation to a DB column allows implementers to configure
	 * encryption for that column. On read and write from this table, the "secret" value will be
	 * decrypted and encrypted, respectively, using a key associated with the specified converter
	 * class. Here, the {@link StringEncryptedAttributeConverter} is used, but implementers can
	 * substitute a custom converter that extends the {@link AbstractEncryptedAttributeConverter}
	 * class.
	 */
	@Column(name = "secret")
	@Convert(converter = StringEncryptedAttributeConverter.class)
	private String secret;

	public void setVuln(int vuln) {
		this.vuln = vuln;
	}

	public int getVuln() {
		return this.vuln;
	}

	public void setProjectModel(ProjectModel project) {
		this.synapseProject = project;
	}

	public ProjectModel getProjectModel() {
		return this.synapseProject;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
