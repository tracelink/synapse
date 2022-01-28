package com.tracelink.prodsec.synapse.scheduler.service;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.model.JobDto;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

/**
 * Handles the business logic to schedule jobs with the Scheduler
 *
 * @author csmith, bhoran
 */
@Service
public class SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final ConcurrentTaskScheduler externalScheduler;
	private final ConcurrentTaskScheduler internalScheduler;
	private final Map<String, JobDto> jobs = new ConcurrentSkipListMap<>();

	private volatile boolean isPaused = false;
	private final ReentrantLock pauseLock = new ReentrantLock();
	private final Condition unpaused = pauseLock.newCondition();

	public SchedulerService(@Autowired ConcurrentTaskScheduler scheduler) {
		this.externalScheduler = scheduler;
		this.internalScheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
	}

	/**
	 * Begin to process a job using the defined schedule
	 *
	 * @param pluginDisplayName the name of the plugin
	 * @param job               the scheduler job configuration to use
	 */
	public void scheduleJob(String pluginDisplayName, SchedulerJob job) {
		// Wait until scheduler service is resumed
		pauseLock.lock();
		try {
			while (isPaused) {
				unpaused.await();
			}
		} catch (InterruptedException ie) {
			return;
		} finally {
			pauseLock.unlock();
		}
		// Schedule job

		String jobKey = pluginDisplayName + job.getJobName();

		Trigger trigger = job.getSchedule().makeTrigger();
		JobDto jobDto = jobs.getOrDefault(jobKey, new JobDto(pluginDisplayName, job.getJobName()));
		jobDto.setNextStartTime(trigger.nextExecutionTime(jobDto));
		jobs.put(jobKey, jobDto);

		this.externalScheduler.schedule(() -> {
			LOG.info("Beginning scheduled job '{}' for plugin '{}'", job.getJobName(),
					pluginDisplayName);
			Runtime instance = Runtime.getRuntime();
			LOG.info("Memory Stats Before - Total: " + instance.totalMemory() + " Free: " + instance.freeMemory() + " Used: " + (instance.totalMemory() - instance.freeMemory()));
			jobDto.setActive(true);
			jobDto.setLastStartTime(new Date());

			job.getJob().run();

			jobDto.setLastEndTime(new Date());
			jobDto.setActive(false);
			jobDto.setNextStartTime(trigger.nextExecutionTime(jobDto));
			LOG.info("Completed scheduled job '{}' for plugin '{}'", job.getJobName(),
					pluginDisplayName);
			LOG.info("Memory Stats After - Total: " + instance.totalMemory() + " Free: " + instance.freeMemory() + " Used: " + (instance.totalMemory() - instance.freeMemory()));
		}, trigger);
	}

	/**
	 * Schedule a Job that can't be modified by users
	 *
	 * @param job the scheduler job configuration to use
	 */
	public void scheduleInternalJob(SchedulerJob job) {
		// Wait until scheduler service is resumed
		pauseLock.lock();
		try {
			while (isPaused) {
				unpaused.await();
			}
		} catch (InterruptedException ie) {
			return;
		} finally {
			pauseLock.unlock();
		}
		// Schedule job
		this.internalScheduler.schedule(() -> {
			LOG.info("Beginning scheduled job '{}'", job.getJobName());
			job.getJob().run();
			LOG.info("Completed scheduled job '{}'", job.getJobName());
		}, job.getSchedule().makeTrigger());
	}

	public Collection<JobDto> getAllJobs() {
		return jobs.values();
	}

	/**
	 * Pause job scheduling
	 */
	public void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * Resume job scheduling
	 */
	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}
}
