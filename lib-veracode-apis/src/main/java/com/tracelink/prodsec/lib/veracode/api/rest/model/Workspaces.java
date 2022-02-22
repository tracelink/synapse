package com.tracelink.prodsec.lib.veracode.api.rest.model;

import java.util.Collections;
import java.util.List;

/**
 * Workspaces
 */
public class Workspaces {

	private List<Workspace> workspaces = Collections.emptyList();

	public List<Workspace> getWorkspaces() {
		return workspaces;
	}

	public void setWorkspaces(List<Workspace> workspaces) {
		this.workspaces = workspaces;
	}

}
