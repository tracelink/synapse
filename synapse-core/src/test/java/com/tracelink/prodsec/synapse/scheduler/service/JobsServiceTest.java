package com.tracelink.prodsec.synapse.scheduler.service;

import static org.mockito.Mockito.mock;

import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.model.JobsModel;
import com.tracelink.prodsec.synapse.scheduler.repo.JobsRepo;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class JobsServiceTest {

	@MockBean
	private JobsRepo mockJobsRepo;

	@Mock
	private SimpleSchedulerJob schedulerJob;

	private JobsModel job;

	private JobsService jobsService;

	@Before
	public void setup() {
		this.jobsService = new JobsService(mockJobsRepo);
		this.job = new JobsModel();

		job.setPluginJobName("Test Plugin : Test Job 1");
		LocalDateTime currTime = LocalDateTime.now();
		job.setStart(currTime.minusMinutes(10));
		job.setFinish(currTime);
	}

	@Test
	public void testCreateJob() {
		BDDMockito.when(schedulerJob.getJobName()).thenReturn("Test Name");
		BDDMockito.when(mockJobsRepo.findByPluginJobName(BDDMockito.anyString())).thenReturn(null);

		jobsService.createJob("Plugin Name", schedulerJob);

		ArgumentCaptor<JobsModel> captor = ArgumentCaptor.forClass(JobsModel.class);
		BDDMockito.verify(mockJobsRepo, Mockito.times(1)).saveAndFlush(captor.capture());
		Assert.assertEquals("Plugin Name : Test Name", captor.getValue().getPluginJobName());
	}

	@Test
	public void testUpdateStartJobs() {
		BDDMockito.when(schedulerJob.getJobName()).thenReturn("Test Scheduler Job");
		BDDMockito.when(mockJobsRepo.findByPluginJobName(BDDMockito.anyString())).thenReturn(job);

		jobsService.updateStartJobs(schedulerJob, "Plugin Name");

		ArgumentCaptor<JobsModel> captor = ArgumentCaptor.forClass(JobsModel.class);
		BDDMockito.verify(mockJobsRepo, Mockito.times(1)).saveAndFlush(captor.capture());
		Assert.assertEquals(job, captor.getValue());
	}

	@Test
	public void testUpdateFinishJobs() {
		BDDMockito.when(schedulerJob.getJobName()).thenReturn("Test Scheduler Job");
		BDDMockito.when(mockJobsRepo.findByPluginJobName(BDDMockito.anyString())).thenReturn(job);

		jobsService.updateFinishJobs(schedulerJob, "Plugin Name");

		ArgumentCaptor<JobsModel> captor = ArgumentCaptor.forClass(JobsModel.class);
		BDDMockito.verify(mockJobsRepo, Mockito.times(1)).saveAndFlush(captor.capture());
		Assert.assertEquals(job, captor.getValue());
	}

	@Test
	public void testGetAllJobs() {
		ScheduledFuture<?> sf = mock(RunnableScheduledFuture.class);
		JobsModel job2 = new JobsModel();

		job2.setPluginJobName("Test Plugin: Test Job2");
		List<JobsModel> jobs = new ArrayList<>();
		jobs.add(job);
		jobs.add(job2);

		// getJobName() used when storing in the HashMap
		BDDMockito.when(schedulerJob.getJobName()).thenReturn("Test Job 1");
		BDDMockito.when(mockJobsRepo.findAllByOrderByPluginJobNameAsc()).thenReturn(jobs);

		jobsService.storeFutureJob(schedulerJob, "Test Plugin", sf);
		List<JobsModel> returnedJobList = jobsService.getAllJobs();

		BDDMockito.verify(mockJobsRepo, Mockito.times(1)).findAllByOrderByPluginJobNameAsc();
		Assert.assertEquals(jobs, returnedJobList);
		// job was stored, job2 wasn't; testing both scenarios
		Assert.assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
				jobs.get(0).getUpcoming().truncatedTo(ChronoUnit.SECONDS));
		Assert.assertEquals(LocalDateTime.MIN, jobs.get(1).getUpcoming());
	}
}
