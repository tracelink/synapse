package com.tracelink.prodsec.synapse.scheduler.model;

import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

public class JobsModelTest {

	@Test
	public void testGetActive() {
		JobsModel job = new JobsModel();
		Assert.assertTrue(job.getActive());

		job.setStart(LocalDateTime.now().minusMinutes(5));
		Assert.assertTrue(job.getActive());

		job.setFinish(LocalDateTime.now().minusMinutes(1));
		Assert.assertFalse(job.getActive());

		job.setStart(LocalDateTime.now());
		Assert.assertTrue(job.getActive());
	}

	@Test
	public void testGetRuntime() {
		JobsModel job = new JobsModel();
		LocalDateTime currTime = LocalDateTime.now();
		Assert.assertEquals("N/A", job.getRuntime());

		job.setStart(currTime.minusMinutes(5));
		Assert.assertEquals("N/A", job.getRuntime());

		job.setFinish(currTime.minusMinutes(1));
		Assert.assertEquals("0:04:00.00", job.getRuntime());

		job.setStart(currTime);
		job.setFinish(currTime);
		Assert.assertEquals("0:00:00.00", job.getRuntime());

		job.setStart(currTime.minusDays(1));
		job.setFinish(currTime.minusMinutes(1));
		Assert.assertEquals("23:59:00.00", job.getRuntime());
	}

	@Test
	public void testDAO() {
		JobsModel job = new JobsModel();
		job.setPluginJobName("Job");
		LocalDateTime currTime = LocalDateTime.now();
		job.setStart(currTime.minusMinutes(5));
		job.setFinish(currTime);

		Assert.assertEquals("Job", job.getPluginJobName());
		Assert.assertEquals(currTime.minusMinutes(5), job.getStart());
		Assert.assertEquals(currTime, job.getFinish());
	}
}
