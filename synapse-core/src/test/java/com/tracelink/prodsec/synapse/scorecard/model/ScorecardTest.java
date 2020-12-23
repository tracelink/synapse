package com.tracelink.prodsec.synapse.scorecard.model;

import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import org.junit.Assert;
import org.junit.Test;

public class ScorecardTest {

	@Test
	public void testDAO() {
		Scorecard scorecard = new Scorecard();
		ScorecardColumn column = new SimpleScorecardColumn("");
		ScorecardRow row = new ScorecardRow("");
		scorecard.addColumn(column);
		scorecard.addRow(row);
		Assert.assertTrue(scorecard.getColumns().contains(column));
		Assert.assertTrue(scorecard.getRows().contains(row));
	}

	@Test
	public void testScorecardFinalClean() {
		ScorecardValue value1 = new ScorecardValue("someval", TrafficLight.GREEN);
		ScorecardRow row1 = new ScorecardRow("row1");
		row1.addOrderedResult(value1);

		ScorecardValue value2 = new ScorecardValue("someval", TrafficLight.GREEN);
		ScorecardRow row2 = new ScorecardRow("row2");
		row2.addOrderedResult(value2);

		ScorecardColumn col1 = new SimpleScorecardColumn("col1");
		Scorecard scorecard = new Scorecard();
		scorecard.addColumn(col1);
		scorecard.addRow(row1);
		scorecard.addRow(row2);

		scorecard.finalizeScorecard();
		Assert.assertTrue(scorecard.getColumns().contains(col1));
	}

	@Test
	public void testScorecardFinalBlankColumn() {
		ScorecardValue value1 = ScorecardValue.BLANK;
		ScorecardRow row1 = new ScorecardRow("row1");
		row1.addOrderedResult(value1);

		ScorecardValue value2 = ScorecardValue.BLANK;
		ScorecardRow row2 = new ScorecardRow("row2");
		row2.addOrderedResult(value2);

		ScorecardColumn col1 = new SimpleScorecardColumn("col1");
		Scorecard scorecard = new Scorecard();
		scorecard.addColumn(col1);
		scorecard.addRow(row1);
		scorecard.addRow(row2);

		scorecard.finalizeScorecard();
		Assert.assertTrue(scorecard.getColumns().isEmpty());
	}

	@Test
	public void testScorecardFinalPartialBlankColumn() {
		ScorecardValue value1 = new ScorecardValue("someval", TrafficLight.GREEN);
		ScorecardRow row1 = new ScorecardRow("row1");
		row1.addOrderedResult(value1);

		ScorecardValue value2 = ScorecardValue.BLANK;
		ScorecardRow row2 = new ScorecardRow("row2");
		row2.addOrderedResult(value2);

		ScorecardColumn col1 = new SimpleScorecardColumn("col1");
		Scorecard scorecard = new Scorecard();
		scorecard.addColumn(col1);
		scorecard.addRow(row1);
		scorecard.addRow(row2);

		scorecard.finalizeScorecard();
		Assert.assertTrue(scorecard.getColumns().contains(col1));
		Assert.assertTrue(scorecard.getRows().contains(row1));
		Assert.assertTrue(scorecard.getRows().contains(row2));
	}
}
