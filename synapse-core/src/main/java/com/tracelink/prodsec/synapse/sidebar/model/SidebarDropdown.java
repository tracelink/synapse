package com.tracelink.prodsec.synapse.sidebar.model;

import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A SidebarDropdown manages the data for a dropdown menu item. This is
 * synthetically created based on the {@link PluginDisplayGroup}
 *
 * @author csmith
 */
public class SidebarDropdown {

	private final PluginDisplayGroup display;
	private final Set<Collection<String>> authorizations = new HashSet<>();

	private static final String DEFAULT_AUTHORIZATION = "isAuthenticated()";

	public SidebarDropdown(PluginDisplayGroup display) {
		this.display = display;
	}

	public String getDisplayName() {
		return this.display.getDisplayName();
	}

	/**
	 * The Material icon (e.g. from https://material.io/resources/icons) for the
	 * dropdown. This comes from the {@link PluginDisplayGroup}
	 *
	 * @return the material icon value
	 */
	public String getMaterialIcon() {
		return this.display.getMaterialIcon();
	}

	/**
	 * Add a collection of required authorizations for this sidebar link.
	 *
	 * @param linkAuthorizations the authorizations to require
	 */
	public void addAuthorization(Collection<String> linkAuthorizations) {
		if (linkAuthorizations == null) {
			return;
		}
		authorizations.add(linkAuthorizations);
	}

	/**
	 * Get the Spring Security expression for this dropdown. This affects the UI
	 * show/hide Authorization, not the controller authorization.
	 *
	 * @return the Spring Security expression for this dropdown. Default
	 * "isAuthenticated()" otherwise "hasAnyAuthority(priv,priv)"
	 */
	public String getAuthorizeExpression() {
		String authorizationExpression;
		/* If there are no link privileges or if any of the links require no privileges, dropdown
		 * should be visible as an authenticated user.
		 */
		if (authorizations.isEmpty() || authorizations.stream().anyMatch(Collection::isEmpty)) {
			authorizationExpression = DEFAULT_AUTHORIZATION;
		} else {
			authorizationExpression = "hasAnyAuthority(";
			// "priv", "priv", ...
			authorizationExpression += authorizations.stream().flatMap(Collection::stream)
					.collect(Collectors.joining("\", \"", "\"", "\""));
			authorizationExpression += ")";
		}
		return authorizationExpression;
	}
}
