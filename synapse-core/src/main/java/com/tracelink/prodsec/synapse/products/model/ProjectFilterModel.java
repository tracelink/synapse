package com.tracelink.prodsec.synapse.products.model;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * ProjectFilter Model/Entity. Defined by name and id. Both must be unique.
 * <p>
 * Also contains a reference to the join table that maps projects and filters to
 * each other
 *
 * @author csmith
 */
@Entity
@Table(name = "projectfilter", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProjectFilterModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "filter_id")
	private long id;

	@Column(name = "name")
	private String name;

	//	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "filter_projects", //
			schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA, //
			joinColumns = @JoinColumn(name = "filter_id"), //
			inverseJoinColumns = @JoinColumn(name = "project_id"))
	@OrderBy("name asc")
	private List<ProjectModel> projects;

	public long getId() {
		return this.id;
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

	public List<String> getProjectNames() {
		if (getProjects().isEmpty()) {
			return new ArrayList<>();
		}
		return getProjects().stream().map(ProjectModel::getName).collect(Collectors.toList());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (other instanceof ProjectFilterModel) {
			return ((ProjectFilterModel) other).getName().equals(getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
