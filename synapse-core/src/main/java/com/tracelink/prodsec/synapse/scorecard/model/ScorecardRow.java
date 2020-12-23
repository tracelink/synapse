package com.tracelink.prodsec.synapse.scorecard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The Scorecard row handles output of a {@link ScorecardValue} for each column
 *
 * @author csmith
 */
public class ScorecardRow {

	private final String rowName;
	private final List<ScorecardValue> results = new ArrayList<>();

	public ScorecardRow(String rowName) {
		this.rowName = rowName;
	}

	public void addOrderedResult(ScorecardValue result) {
		results.add(result);
	}

	public String getRowName() {
		return this.rowName;
	}

	public List<ScorecardValue> getResults() {
		return results;
	}
}
