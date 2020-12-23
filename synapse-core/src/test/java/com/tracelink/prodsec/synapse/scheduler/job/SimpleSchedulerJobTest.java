package com.tracelink.prodsec.synapse.scheduler.job;

import com.tracelink.prodsec.synapse.scheduler.service.schedule.ISchedule;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class SimpleSchedulerJobTest {

	@Test
	public void testDAO() {
		String jobName = "jobName";
		Runnable job = () -> System.out.println("foobar");
		ISchedule schedule = new PeriodicSchedule(1, TimeUnit.DAYS);
		SimpleSchedulerJob scheduledJob = new SimpleSchedulerJob(jobName).withJob(job)
				.onSchedule(schedule);

		Assert.assertEquals(jobName, scheduledJob.getJobName());
		Assert.assertEquals(job, scheduledJob.getJob());
		Assert.assertEquals(schedule, scheduledJob.getSchedule());
	}
}
