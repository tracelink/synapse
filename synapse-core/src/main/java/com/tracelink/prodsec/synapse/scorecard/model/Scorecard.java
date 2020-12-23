package com.tracelink.prodsec.synapse.scorecard.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The scorecard object that manages the scorecard table
 *
 * @author csmith
 */
public class Scorecard {

	private final List<ScorecardColumn> columns = new ArrayList<>();
	private final List<ScorecardRow> rows = new ArrayList<>();

	public void addColumn(ScorecardColumn column) {
		this.columns.add(column);
	}

	public void addRow(ScorecardRow row) {
		this.rows.add(row);
	}

	public List<ScorecardColumn> getColumns() {
		return columns;
	}

	public List<ScorecardRow> getRows() {
		return rows;
	}

	/**
	 * This method ensures that empty columns are skipped entirely. This happens
	 * when a column doesn't have data for the projects or productlines in a
	 * filtered view
	 */
	public void finalizeScorecard() {
		List<Integer> columnUsage = new ArrayList<>(Collections.nCopies(columns.size(), 0));

		for (ScorecardRow row : rows) {
			int size = row.getResults().size();
			for (int i = 0; i < size; i++) {
				ScorecardValue v = row.getResults().get(i);
				if (v != ScorecardValue.BLANK) {
					columnUsage.set(i, columnUsage.get(i) + 1);
				}
			}
		}
		Iterator<ScorecardColumn> colIter = columns.iterator();
		Iterator<Integer> useIter = columnUsage.iterator();
		int index = 0;
		while (useIter.hasNext()) {
			int usage = useIter.next();
			colIter.next();
			if (usage == 0) {
				colIter.remove();
				for (ScorecardRow row : rows) {
					row.getResults().remove(index);
				}
				index--;
			}

			index++;
		}
	}
}
