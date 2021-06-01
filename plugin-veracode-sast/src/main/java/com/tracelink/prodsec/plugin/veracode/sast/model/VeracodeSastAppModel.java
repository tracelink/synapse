package com.tracelink.prodsec.plugin.veracode.sast.model;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * The App Model links the Veracode App to the Synapse Project and has one or
 * more Veracode Reports associated with it
 *
 * @author csmith
 */
@Entity
@Table(schema = VeracodeSastPlugin.SCHEMA, name = "veracode_sast_apps")
public class VeracodeSastAppModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "app_id")
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "product_name")
	private String productLineName;

	@Column(name = "model_type")
	@Convert(converter = ModelType.ModelTypeConverter.class)
	private ModelType modelType;

	/**
	 * Whether the reports and flaws associated with this app should be included by Synapse. If
	 * excluded, this app and the reports and flaws associated with it will not be displayed in
	 * graphs, summary statistics, or the flaws page.
	 */
	@Column(name = "included")
	private boolean included = true;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synapse_project")
	private ProjectModel synapseProject;

	@OneToMany(mappedBy = "app", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("reportDate desc")
	private List<VeracodeSastReportModel> reports = new ArrayList<>();

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProductLineName() {
		return productLineName;
	}

	public void setProductLineName(String productLineName) {
		this.productLineName = productLineName;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public ProjectModel getSynapseProject() {
		return synapseProject;
	}

	public void setSynapseProject(ProjectModel synapseProject) {
		this.synapseProject = synapseProject;
	}

	public List<VeracodeSastReportModel> getReports() {
		return reports;
	}

	public void setReports(List<VeracodeSastReportModel> reports) {
		this.reports = reports;
	}

	public boolean isVulnerable() {
		VeracodeSastReportModel current = getCurrentReport();
		if (current == null) {
			return false;
		}
		return getCurrentReport().getScore() < 100;
	}

	public VeracodeSastReportModel getCurrentReport() {
		return (reports == null || reports.isEmpty()) ? null : reports.get(0);
	}

	public VeracodeSastReportModel getOldestReport() {
		return (reports == null || reports.isEmpty()) ? null : reports.get(reports.size() - 1);
	}

	/**
	 * Gets the display name for this app model based on whether it is an app or a sandbox.
	 *
	 * @return display name including app and/or sandbox name
	 */
	public String getDisplayName() {
		if (modelType.equals(ModelType.APP)) {
			return name;
		} else {
			return productLineName + " - " + name;
		}
	}

}
