package com.tracelink.prodsec.synapse.spi;

/**
 * A simple data object to manage the Plugin's name and primary icon
 * 
 * @author csmith
 *
 */
public class PluginDisplayGroup {
	private final String displayName;
	private final String materialIcon;

	public PluginDisplayGroup(String displayName, String materialIcon) {
		this.displayName = displayName;
		this.materialIcon = materialIcon;
	}

	/**
	 * @return the name used in all UI displays and as keys in some data structures.
	 *         Must be unique
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the material icon (https://material.io/resources/icons/) used in all
	 *         UI displays
	 */
	public String getMaterialIcon() {
		return materialIcon;
	}

}
