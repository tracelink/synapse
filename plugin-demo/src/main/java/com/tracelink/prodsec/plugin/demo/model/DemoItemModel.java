package com.tracelink.prodsec.plugin.demo.model;

/**
 * The DemoItem is a DAO holder to make the UI much easier to handle.
 * 
 * @author csmith
 *
 */
public class DemoItemModel {
	private final String projectName;
	private final boolean isConfigured;
	private final int vulns;

	/**
	 * Create the ItemModel
	 * 
	 * @param projectName  the project this is attached to
	 * @param isConfigured if true, vulns is assumed configured. If false, vulns is
	 *                     ignored
	 * @param vulns        the number of vulns this project has
	 */
	public DemoItemModel(String projectName, boolean isConfigured, int vulns) {
		this.projectName = projectName;
		this.isConfigured = isConfigured;
		this.vulns = vulns;
	}

	public String getProjectName() {
		return projectName;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

	public int getVulns() {
		return vulns;
	}

}
