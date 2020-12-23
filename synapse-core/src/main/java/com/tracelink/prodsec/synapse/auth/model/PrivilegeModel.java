package com.tracelink.prodsec.synapse.auth.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;

/**
 * Privilege Model/Entity. Defined by name and id. Both must be unique
 * 
 * @author csmith
 *
 */
@Entity
@Table(name = "privileges", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
public class PrivilegeModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "privilege_id")
	private long id;

	@Column(name = "name")
	private String name;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
