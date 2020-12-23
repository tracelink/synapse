package com.tracelink.prodsec.synapse.scheduler.service;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.model.JobsModel;
import com.tracelink.prodsec.synapse.scheduler.repo.JobsRepo;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles business logic to maintain information about jobs and their
 * most recent runs
 *
 * @author bhoran
 */
@Service
public class JobsService {

	private final HashMap<String, ScheduledFuture<?>> futureJobs = new HashMap<>();

	private final JobsRepo jobsRepo;

	private static final Logger LOGGER = LoggerFactory.getLogger(JobsService.class);


	public JobsService(@Autowired JobsRepo jobsRepo) {
		this.jobsRepo = jobsRepo;
	}

	public void createJob(String pluginName, SchedulerJob job) {
		String jobName = concatPluginJobName(pluginName, job.getJobName());
		JobsModel storedJob = jobsRepo.findByPluginJobName(jobName);
		if (storedJob == null) {
			storedJob = new JobsModel();
			storedJob.setPluginJobName(jobName);
		}
		jobsRepo.saveAndFlush(storedJob);
	}

	public void updateStartJobs(SchedulerJob job, String pluginName) {
		JobsModel storedJob = jobsRepo
				.findByPluginJobName(concatPluginJobName(pluginName, job.getJobName()));
		storedJob.setStart(LocalDateTime.now());
		jobsRepo.saveAndFlush(storedJob);
	}

	public void updateFinishJobs(SchedulerJob job, String pluginName) {
		JobsModel storedJob = jobsRepo
				.findByPluginJobName(concatPluginJobName(pluginName, job.getJobName()));
		storedJob.setFinish(LocalDateTime.now());
		jobsRepo.saveAndFlush(storedJob);
	}

	public List<JobsModel> getAllJobs() {
		List<JobsModel> jobs = jobsRepo.findAllByOrderByPluginJobNameAsc();

		for (JobsModel job : jobs) {
			String jobName = job.getPluginJobName();
			if (futureJobs.containsKey(jobName)) {
				ScheduledFuture<?> future = futureJobs.get(jobName);
				job.setUpcoming(LocalDateTime.now().plusSeconds(future.getDelay(TimeUnit.SECONDS)));
			} else {
				LOGGER.error("Error retrieving " + jobName
						+ ", unable to update time before the next job is run.");
				job.setUpcoming(LocalDateTime.MIN);
			}
		}
		return jobs;
	}

	public void storeFutureJob(SchedulerJob job, String pluginName, ScheduledFuture<?> future) {
		String name = concatPluginJobName(pluginName, job.getJobName());
		futureJobs.put(name, future);
	}

	private String concatPluginJobName(String plugin, String job) {
		return plugin + " : " + job;
	}
}
