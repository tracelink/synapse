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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * ProductLine Model/Entity. Defined by name and id. Both must be unique.
 * <p>
 * Also contains a reference to all projects where this is the parent
 * ProductLine
 *
 * @author csmith
 */
@Entity
@Table(name = "productline", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductLineModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "productline_id")
	private long id;

	@Column(name = "name")
	private String name;

	//	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(mappedBy = "owningProductLine", fetch = FetchType.EAGER)
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
		if (other instanceof ProductLineModel) {
			return ((ProductLineModel) other).getName().equals(getName());
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
