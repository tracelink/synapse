package com.tracelink.prodsec.synapse.scheduler.service;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.ISchedule;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SchedulerServiceTest {

	@MockBean
	private ConcurrentTaskScheduler mockScheduler;

	@MockBean
	private JobsService jobsService;

	private SchedulerService schedulerService;

	@Before
	public void setup() {
		this.schedulerService = new SchedulerService(mockScheduler, jobsService);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void scheduleJobTestSuccess() {
		String pluginName = "foobar";
		String jobName = "jobName";

		Runnable job = () -> {
		};
		ISchedule schedule = new PeriodicSchedule(1, TimeUnit.DAYS);
		SchedulerJob scheduledJob = new SimpleSchedulerJob(jobName).onSchedule(schedule)
				.withJob(job);

		ScheduledFuture mockFuture = BDDMockito.mock(ScheduledFuture.class);

		BDDMockito.when(mockScheduler
				.schedule(BDDMockito.any(Runnable.class), BDDMockito.any(Trigger.class)))
				.thenReturn(mockFuture);

		schedulerService.scheduleJob(pluginName, scheduledJob);
		BDDMockito.verify(jobsService)
				.createJob(BDDMockito.anyString(), BDDMockito.any(SchedulerJob.class));
		BDDMockito.verify(mockScheduler)
				.schedule(BDDMockito.any(Runnable.class), BDDMockito.any(Trigger.class));
		BDDMockito.verify(jobsService)
				.storeFutureJob(BDDMockito.any(SchedulerJob.class), BDDMockito.anyString(),
						BDDMockito.any(ScheduledFuture.class));
	}

	@Test
	public void scheduleJobTestRunJob() throws InterruptedException {
		String pluginName = "foobar";
		String jobName = "jobName";

		Runnable mockJob = BDDMockito.mock(Runnable.class);
		ISchedule schedule = new PeriodicSchedule(1, TimeUnit.DAYS);
		SchedulerJob scheduledJob = new SimpleSchedulerJob(jobName).onSchedule(schedule)
				.withJob(mockJob);

		ConcurrentTaskScheduler realScheduler = new ConcurrentTaskScheduler(
				Executors.newScheduledThreadPool(2));
		SchedulerService schedulerService = new SchedulerService(realScheduler, jobsService);

		// first schedule and add to the map
		schedulerService.scheduleJob(pluginName, scheduledJob);
		Thread.sleep(500);
		BDDMockito.verify(mockJob).run();

		// second schedule to confirm repeatability
		schedulerService.scheduleJob(pluginName, scheduledJob);
		Thread.sleep(500);
		BDDMockito.verify(mockJob, BDDMockito.times(2)).run();
	}

	@Test
	public void scheduleKeyRotationJob() throws InterruptedException {
		String jobName = "jobName";

		Runnable mockJob = BDDMockito.mock(Runnable.class);
		ISchedule schedule = new PeriodicSchedule(1, TimeUnit.DAYS);
		SchedulerJob scheduledJob = new SimpleSchedulerJob(jobName).onSchedule(schedule)
				.withJob(mockJob);

		ConcurrentTaskScheduler realScheduler = new ConcurrentTaskScheduler(
				Executors.newScheduledThreadPool(2));
		SchedulerService schedulerService = new SchedulerService(realScheduler, jobsService);

		// first schedule and add to the map
		schedulerService.scheduleKeyRotationJob(scheduledJob);
		Thread.sleep(500);
		BDDMockito.verify(mockJob).run();

		// second schedule to confirm repeatability
		schedulerService.scheduleKeyRotationJob(scheduledJob);
		Thread.sleep(500);
		BDDMockito.verify(mockJob, BDDMockito.times(2)).run();
	}
}
