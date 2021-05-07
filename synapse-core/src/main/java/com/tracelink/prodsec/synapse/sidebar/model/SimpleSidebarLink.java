package com.tracelink.prodsec.synapse.sidebar.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * A Simple implementation of the {@link SidebarLink} that uses Spring Security
 * to create authorization expressions and is otherwise a DAO
 *
 * @author csmith
 */
public class SimpleSidebarLink implements SidebarLink {

	private static final String DEFAULT_AUTHORIZATION = "isAuthenticated()";

	private String authorize;
	private String pageLink;
	private final String displayName;
	private String materialIcon;
	private final Collection<String> privs = new HashSet<>();

	public SimpleSidebarLink(String displayName) {
		this.displayName = displayName;
		this.authorize = DEFAULT_AUTHORIZATION;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Sets the required privileges for this sidebar link and returns this.
	 *
	 * @param privileges the privileges to set
	 * @return this sidebar link
	 */
	public SimpleSidebarLink withPrivileges(String... privileges) {
		if (privileges == null) {
			return this;
		}
		privs.addAll(Arrays.asList(privileges));

		return this;
	}

	/**
	 * Get the Spring Security expression for this dropdown. This affects the UI
	 * show/hide Authorization, not the controller authorization.
	 *
	 * @return the Spring Security expression for this dropdown. Default
	 * "isAuthenticated()" otherwise "hasAnyAuthority(priv,priv)"
	 */
	public String getAuthorizeExpression() {
		if (privs.isEmpty()) {
			authorize = DEFAULT_AUTHORIZATION;
		} else {
			this.authorize = "hasAnyAuthority(";
			// "priv", "priv", ...
			this.authorize += privs.stream().collect(Collectors.joining("\", \"", "\"", "\""));
			this.authorize += ")";
		}
		return authorize;
	}

	public Collection<String> getAuthorizePrivileges() {
		return privs;
	}

	/**
	 * Sets the page link for this sidebar link and returns this.
	 *
	 * @param pageLink the page link to set
	 * @return this sidebar link
	 */
	public SimpleSidebarLink withPageLink(String pageLink) {
		this.pageLink = pageLink;
		return this;
	}

	public String getPageLink() {
		return pageLink;
	}

	/**
	 * Sets the material icon for this sidebar link and returns this.
	 *
	 * @param materialIcon the material icon to set
	 * @return this sidebar link
	 */
	public SimpleSidebarLink withMaterialIcon(String materialIcon) {
		this.materialIcon = materialIcon;
		return this;
	}

	public String getMaterialIcon() {
		return materialIcon;
	}
}
