package com.tracelink.prodsec.synapse.auth.model;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import java.util.Collection;
import java.util.HashSet;
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
import org.apache.commons.lang3.StringUtils;

/**
 * User Model/Entity. Defined by email and id. Both must be unique
 * <p>
 * Additionally has password and enabled fields to track the user's password and
 * whether the user can log in
 * <p>
 * Has a many-to-many relationship with roles using a join table.
 *
 * @author csmith
 */
@Entity
@Table(name = "users", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
public class UserModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private long id;

	@Column(name = "sso_id")
	private String ssoId;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "enabled")
	private boolean enabled;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles", //
			schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA, //
			joinColumns = @JoinColumn(name = "user_id"), //
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Collection<RoleModel> roles = new HashSet<>();

	public long getId() {
		return id;
	}

	public String getSsoId() {
		return ssoId;
	}

	public void setSsoId(String ssoId) {
		this.ssoId = ssoId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Collection<RoleModel> getRoles() {
		return roles;
	}

	public void setRoles(Collection<RoleModel> roles) {
		this.roles = roles;
	}

	public String getRolesAsString() {
		return roles.stream().map(RoleModel::getRoleName).collect(Collectors.joining(", "));
	}

	public boolean hasRole(String roleName) {
		return roles.stream().anyMatch(r -> r.getRoleName().equals(roleName));
	}

	/**
	 * Determines whether this user is an SSO user. If this user is not an SSO user, they are a
	 * local user.
	 *
	 * @return true if the user is an SSO user, false if the user is a local user
	 */
	public boolean isSsoUser() {
		return StringUtils.isNotBlank(getSsoId());
	}

}
