package com.tracelink.prodsec.synapse.scorecard.model;

import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import java.util.function.Function;
import org.junit.Assert;
import org.junit.Test;

public class SimpleScorecardColumnTest {

	@Test
	public void testDAO() {
		String pageLink = "pageLink";
		String columnName = "columnName";
		Function<ProjectModel, ScorecardValue> projectCallback = (project) -> new ScorecardValue(
				project.getName(),
				TrafficLight.GREEN);
		Function<ProductLineModel, ScorecardValue> productLineCallback = (productLine) -> new ScorecardValue(
				productLine.getName(), TrafficLight.GREEN);

		SimpleScorecardColumn column = new SimpleScorecardColumn(columnName).withPageLink(pageLink);

		Assert.assertEquals(columnName, column.getColumnName());
		Assert.assertEquals(pageLink, column.getPageLink());
		Assert.assertFalse(column.hasProjectCallback());
		Assert.assertFalse(column.hasProductLineCallback());

		column.withProductLineCallback(productLineCallback).withProjectCallback(projectCallback);

		Assert.assertTrue(column.hasProjectCallback());
		Assert.assertTrue(column.hasProductLineCallback());

		Assert.assertEquals(productLineCallback, column.getProductLineCallbackFunction());
		Assert.assertEquals(projectCallback, column.getProjectCallbackFunction());
	}
}
