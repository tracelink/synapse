package com.tracelink.prodsec.synapse.products.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;

/**
 * Project Model/Entity. Defined by name and id. Both must be unique.
 * 
 * Also owns the link between this project and its required productline parent.
 * 
 * Also contains a reference to the join table that maps projects and filters to
 * each other
 * 
 * @author csmith
 *
 */
@Entity
@Table(name = "project", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProjectModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "project_id")
	private long id;

	@Column(name = "name")
	private String name;

//	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owning_productline", nullable = false)
	private ProductLineModel owningProductLine;

//	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(mappedBy = "projects", fetch = FetchType.EAGER)
	@OrderBy("name asc")
	private List<ProjectFilterModel> filters;

	public long getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setOwningProductLine(ProductLineModel owningProductLine) {
		this.owningProductLine = owningProductLine;
	}

	public ProductLineModel getOwningProductLine() {
		return this.owningProductLine;
	}

	public void setFilters(List<ProjectFilterModel> filters) {
		this.filters = filters;
	}

	public List<ProjectFilterModel> getFilters() {
		return this.filters;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (other instanceof ProjectModel) {
			return ((ProjectModel) other).getName().equals(getName());
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
