package com.tracelink.prodsec.lib.veracode.rest.api.model;

import java.util.Collections;
import java.util.List;

/**
 * Projects
 */
public class Projects {

	private List<Project> projects = Collections.emptyList();

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
}
