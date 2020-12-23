package com.tracelink.prodsec.plugin.sme.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.plugin.sme.SMEPlugin;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

@Entity
@Table(schema = SMEPlugin.SCHEMA, name = "sme_names")
public class SMEEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sme_id")
	private long id;

	@Column(name = "name")
	private String name;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "sme_project", //
			schema = SMEPlugin.SCHEMA, //
			joinColumns = @JoinColumn(name = "sme_id"), //
			inverseJoinColumns = @JoinColumn(name = "project_id"))
	@OrderBy("name asc")
	private List<ProjectModel> projects;

	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setProjects(List<ProjectModel> projects) {
		this.projects = projects;
	}

	public List<ProjectModel> getProjects() {
		return this.projects;
	}
}
