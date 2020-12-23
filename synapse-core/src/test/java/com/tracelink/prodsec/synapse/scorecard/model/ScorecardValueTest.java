package com.tracelink.prodsec.synapse.scorecard.model;

import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import org.junit.Assert;
import org.junit.Test;

public class ScorecardValueTest {

	@Test
	public void testDAO() {
		TrafficLight color = TrafficLight.RED;
		String value = "value";

		ScorecardValue scorecardValue = new ScorecardValue(value, color);

		Assert.assertEquals(value, scorecardValue.getValue());
		Assert.assertEquals(color, scorecardValue.getColor());
		Assert.assertEquals(TrafficLight.RED.getColor(), scorecardValue.getColor().getColor());
	}
}
