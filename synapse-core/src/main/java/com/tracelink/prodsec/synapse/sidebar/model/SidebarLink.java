package com.tracelink.prodsec.synapse.sidebar.model;

import java.util.Collection;

/**
 * Sidebar Link interface to help UI connect the sidebar to controllers
 * 
 * @author csmith
 *
 */
public interface SidebarLink {

	/**
	 * @return The UI display name for this link
	 */
	String getDisplayName();

	/**
	 * @return the Authorize Expression to show/hide this link element
	 */
	String getAuthorizeExpression();

	/**
	 * @return all privileges needed to show/hide this link
	 */
	Collection<String> getAuthorizePrivileges();

	/**
	 * @return the URL that this link takes users to
	 */
	String getPageLink();

	/**
	 * @return the material (https://material.io/resources/icons/) icon for this
	 *         link
	 */
	String getMaterialIcon();
}
