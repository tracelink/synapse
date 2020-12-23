package com.tracelink.prodsec.synapse.scorecard.model;

import java.util.function.Function;

import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

/**
 * The Scorecard Column interface handles the column headers, links, and
 * callbacks for relevant objects (Project or ProductLine)
 * 
 * @author csmith
 *
 */
public interface ScorecardColumn {
	String getColumnName();

	String getPageLink();

	default boolean hasProjectCallback() {
		return getProjectCallbackFunction() != null;
	}

	Function<ProjectModel, ScorecardValue> getProjectCallbackFunction();

	default boolean hasProductLineCallback() {
		return getProductLineCallbackFunction() != null;
	}

	Function<ProductLineModel, ScorecardValue> getProductLineCallbackFunction();
}
