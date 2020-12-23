package com.tracelink.prodsec.plugin.veracode.dast.model;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
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
 * The App Model links the Veracode App to the Synapse Product Line and has one or more Veracode
 * Reports associated with it
 *
 * @author csmith
 */
@Entity
@Table(schema = VeracodeDastPlugin.SCHEMA, name = "veracode_dast_apps")
public class VeracodeDastAppModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "app_id")
	private long id;

	@Column(name = "name")
	private String name;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synapse_product_line")
	private ProductLineModel synapseProductLine;

	@OneToMany(mappedBy = "app", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("reportDate desc")
	private List<VeracodeDastReportModel> reports = new ArrayList<>();

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProductLineModel getSynapseProductLine() {
		return synapseProductLine;
	}

	public void setSynapseProductLine(ProductLineModel synapseProductLine) {
		this.synapseProductLine = synapseProductLine;
	}

	public List<VeracodeDastReportModel> getReports() {
		return reports;
	}

	public void setReports(List<VeracodeDastReportModel> reports) {
		this.reports = reports;
	}

	public boolean isVulnerable() {
		VeracodeDastReportModel current = getCurrentReport();
		if (current == null) {
			return false;
		}
		return getCurrentReport().getScore() < 100;
	}

	public VeracodeDastReportModel getCurrentReport() {
		return (reports == null || reports.isEmpty()) ? null : reports.get(0);
	}

	public VeracodeDastReportModel getOldestReport() {
		return (reports == null || reports.isEmpty()) ? null : reports.get(reports.size() - 1);
	}

}
