package com.tracelink.prodsec.synapse.scorecard.model;

import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.util.function.Function;

/**
 * A simple DAO implementation of the {@link ScorecardColumn}
 *
 * @author csmith
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

	/**
	 * Sets the page link for this column and returns this.
	 *
	 * @param pageLink the page link to set
	 * @return this scorecard column
	 */
	public SimpleScorecardColumn withPageLink(String pageLink) {
		this.pageLink = pageLink;
		return this;
	}

	public String getPageLink() {
		return pageLink;
	}

	/**
	 * Sets the project callback function for this column and returns this.
	 *
	 * @param projectCallback the project callback function to set
	 * @return this scorecard column
	 */
	public SimpleScorecardColumn withProjectCallback(
			Function<ProjectModel, ScorecardValue> projectCallback) {
		this.projectCallback = projectCallback;
		return this;
	}

	public Function<ProjectModel, ScorecardValue> getProjectCallbackFunction() {
		return this.projectCallback;
	}

	/**
	 * Sets the product line callback function for this column and returns this.
	 *
	 * @param productLineCallback the product line callback function to set
	 * @return this scorecard column
	 */
	public SimpleScorecardColumn withProductLineCallback(
			Function<ProductLineModel, ScorecardValue> productLineCallback) {
		this.productLineCallback = productLineCallback;
		return this;
	}

	public Function<ProductLineModel, ScorecardValue> getProductLineCallbackFunction() {
		return this.productLineCallback;
	}
}
