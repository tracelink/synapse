package com.tracelink.prodsec.synapse.scheduler.model;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class JobsModelTest {

	@Test
	public void testDAO() {
		String pluginName = "plugin";
		String jobName = "job";

		JobDto job = new JobDto(pluginName, jobName);

		Assert.assertFalse(job.isActive());
		job.setActive(true);
		Assert.assertTrue(job.isActive());

		Date start = new Date(1234);
		Date finish = new Date(2345);
		Date next = new Date(3456);

		job.setLastStartTime(start);
		job.setLastEndTime(finish);
		job.setNextStartTime(next);

		Assert.assertEquals(start, job.getLastStartTime());
		Assert.assertEquals(start, job.lastActualExecutionTime());
		Assert.assertEquals(finish, job.getLastEndTime());
		Assert.assertEquals(finish, job.lastCompletionTime());
		Assert.assertEquals(next, job.getNextStartTime());
		Assert.assertEquals(next, job.lastScheduledExecutionTime());
	}

	@Test
	public void testDurations() {
		String pluginName = "plugin";
		String jobName = "job";

		JobDto job = new JobDto(pluginName, jobName);
		Date start = new Date(1234);
		Date finish = new Date(2345);
		job.setLastStartTime(start);
		job.setLastEndTime(finish);
		
		Assert.assertEquals(finish.getTime()-start.getTime(), job.getDurationMs());
		Assert.assertEquals("0:01.111", job.getDurationString());
	}

	@Test
	public void testActiveJobDurationString() {
		String pluginName = "plugin";
		String jobName = "job";

		JobDto job = new JobDto(pluginName, jobName);
		Date start = new Date(2345);
		Date finish = new Date(1234);
		job.setLastStartTime(start);
		job.setLastEndTime(finish);

		Assert.assertEquals("N/A", job.getDurationString());
	}
}
