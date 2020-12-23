package com.tracelink.prodsec.synapse.scorecard.model;

import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import org.junit.Assert;
import org.junit.Test;

public class ScorecardRowTest {

	@Test
	public void testDAO() {
		String rowName = "rowName";
		ScorecardValue value = new ScorecardValue("", TrafficLight.GREEN);
		ScorecardRow row = new ScorecardRow(rowName);
		row.addOrderedResult(value);

		Assert.assertEquals(rowName, row.getRowName());
		Assert.assertTrue(row.getResults().contains(value));
	}
}
