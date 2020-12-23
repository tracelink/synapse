package com.tracelink.prodsec.synapse.auth.model;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;

/**
 * Role Model/Entity. Defined by name and id. Both must be unique.
 * 
 * Has a many-to-many relationship with privileges using a join table.
 * 
 * @author csmith
 *
 */
@Entity
@Table(name = "roles", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
public class RoleModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private long id;

	@Column(name = "name")
	private String roleName;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "roles_privs", //
			schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA, //
			joinColumns = @JoinColumn(name = "role_id"), //
			inverseJoinColumns = @JoinColumn(name = "privilege_id"))
	private Collection<PrivilegeModel> privileges;

	public long getId() {
		return id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Collection<PrivilegeModel> getPrivileges() {
		return privileges;
	}

	public void setPrivileges(Collection<PrivilegeModel> privileges) {
		this.privileges = privileges;
	}

	public String getPrivilegesAsString() {
		return privileges.stream().map(PrivilegeModel::getName).collect(Collectors.joining(", "));
	}

	public boolean hasPrivilege(String privilegeName) {
		return privileges.stream().anyMatch(p -> p.getName().equals(privilegeName));
	}

}
