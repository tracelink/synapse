package com.tracelink.prodsec.synapse.scheduler.service;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.model.JobDto;

/**
 * Handles the business logic to schedule jobs with the Schedulers
 *
 * @author csmith, bhoran
 */
@Service
public class SchedulerService {
	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final ConcurrentTaskScheduler pluginScheduler;
	private final ConcurrentTaskScheduler coreScheduler;
	private final Map<String, JobDto> jobs = new ConcurrentSkipListMap<>();

	public SchedulerService() {
		this.pluginScheduler = new ConcurrentTaskScheduler(new PauseableScheduledThreadPoolExecutor(1));
		this.coreScheduler = new ConcurrentTaskScheduler(new PauseableScheduledThreadPoolExecutor(1));
		pause();
	}

	/**
	 * Begin to process a job using the defined schedule
	 *
	 * @param pluginDisplayName the name of the plugin
	 * @param job               the scheduler job configuration to use
	 */
	public void scheduleJob(String pluginDisplayName, SchedulerJob job) {
		String jobKey = pluginDisplayName + job.getJobName();

		Trigger trigger = job.getSchedule().makeTrigger();
		JobDto jobDto = jobs.getOrDefault(jobKey, new JobDto(pluginDisplayName, job.getJobName()));
		jobDto.setNextStartTime(trigger.nextExecutionTime(jobDto));

		ScheduledFuture<?> future = this.pluginScheduler.schedule(() -> {
			LOG.info("Beginning scheduled job '{}' for plugin '{}'", job.getJobName(), pluginDisplayName);
			jobDto.setActive(true);
			jobDto.setLastStartTime(new Date());

			job.getJob().run();

			jobDto.setLastEndTime(new Date());
			jobDto.setActive(false);
			jobDto.setNextStartTime(trigger.nextExecutionTime(jobDto));
			LOG.info("Completed scheduled job '{}' for plugin '{}'", job.getJobName(), pluginDisplayName);
		}, trigger);
		jobDto.setFuture(future);
		jobs.put(jobKey, jobDto);
	}

	/**
	 * Remove a job from the schedulers
	 * 
	 * @param displayName the name of the plugin that owns this job
	 * @param job         the job itself
	 */
	public void unscheduleJob(String displayName, SchedulerJob job) {
		String jobKey = displayName + job.getJobName();
		JobDto jobDto = this.jobs.remove(jobKey);
		jobDto.getFuture().cancel(true);
	}

	/**
	 * Schedule a Job that can't be modified by users
	 *
	 * @param job the scheduler job configuration to use
	 */
	public void scheduleCoreJob(SchedulerJob job) {
		this.coreScheduler.schedule(() -> {
			LOG.info("Beginning scheduled job '{}'", job.getJobName());
			job.getJob().run();
			LOG.info("Completed scheduled job '{}'", job.getJobName());
		}, job.getSchedule().makeTrigger());
	}

	public Collection<JobDto> getAllJobs() {
		return jobs.values();
	}

	/**
	 * halts processing of new tasks in both plugin and core schedulers
	 */
	public void pause() {
		((PauseableScheduledThreadPoolExecutor) this.pluginScheduler.getConcurrentExecutor()).pause();
		((PauseableScheduledThreadPoolExecutor) this.coreScheduler.getConcurrentExecutor()).pause();
	}

	/**
	 * resumes processing of new tasks in both plugin and core schedulers
	 */
	public void resume() {
		((PauseableScheduledThreadPoolExecutor) this.pluginScheduler.getConcurrentExecutor()).resume();
		((PauseableScheduledThreadPoolExecutor) this.coreScheduler.getConcurrentExecutor()).resume();
	}

	/**
	 * After startup, the schedulers will be resumed
	 * 
	 * @param event the startup event
	 */
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		resume();
	}

}
