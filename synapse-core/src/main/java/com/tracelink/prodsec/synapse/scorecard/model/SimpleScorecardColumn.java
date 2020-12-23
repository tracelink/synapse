package com.tracelink.prodsec.synapse.scorecard.model;

import java.util.function.Function;

import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * A simple DAO implementation of the {@link ScorecardColumn}
 * 
 * @author csmith
 *
 */
public class SimpleScorecardColumn implements ScorecardColumn {
	private String pageLink;
	private final String columnName;
	private Function<ProjectModel, ScorecardValue> projectCallback;
	private Function<ProductLineModel, ScorecardValue> productLineCallback;

	public SimpleScorecardColumn(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public SimpleScorecardColumn withPageLink(String pageLink) {
		this.pageLink = pageLink;
		return this;
	}

	public String getPageLink() {
		return pageLink;
	}

	public SimpleScorecardColumn withProjectCallback(Function<ProjectModel, ScorecardValue> projectCallback) {
		this.projectCallback = projectCallback;
		return this;
	}

	public Function<ProjectModel, ScorecardValue> getProjectCallbackFunction() {
		return this.projectCallback;
	}

	public SimpleScorecardColumn withProductLineCallback(
			Function<ProductLineModel, ScorecardValue> productLineCallback) {
		this.productLineCallback = productLineCallback;
		return this;
	}

	public Function<ProductLineModel, ScorecardValue> getProductLineCallbackFunction() {
		return this.productLineCallback;
	}
}
