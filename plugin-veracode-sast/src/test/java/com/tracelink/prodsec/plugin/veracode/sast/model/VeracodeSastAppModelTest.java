package com.tracelink.prodsec.plugin.veracode.sast.model;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class VeracodeSastAppModelTest {

	@Test
	public void testIsVulnerableNoReports() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		Assert.assertFalse(app.isVulnerable());
	}

	@Test
	public void testIsVulnerable() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		app.setReports(Collections.singletonList(report));

		report.setScore(100L);
		Assert.assertFalse(app.isVulnerable());
		report.setScore(99L);
		Assert.assertTrue(app.isVulnerable());
	}

	@Test
	public void testGetDisplayNameApp() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		app.setName("App1");

		Assert.assertEquals("App1", app.getDisplayName());
	}
}
