package com.tracelink.prodsec.synapse.scheduler.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.model.JobDto;

/**
 * Handles the business logic to schedule jobs with the Scheduler
 *
 * @author csmith, bhoran
 */
@Service
public class SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final ConcurrentTaskScheduler scheduler;
	private final Map<String, JobDto> jobs = new ConcurrentSkipListMap<>();

	private volatile boolean isPaused = false;
	private final ReentrantLock pauseLock = new ReentrantLock();
	private final Condition unpaused = pauseLock.newCondition();

	public SchedulerService(@Autowired ConcurrentTaskScheduler scheduler) {
		this.scheduler = scheduler;
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

		this.scheduler.schedule(() -> {
			LOG.info("Beginning Scheduled Job {} for Plugin {}", job.getJobName(), pluginDisplayName);

			jobDto.setActive(true);
			jobDto.setLastStartTime(new Date());

			job.getJob().run();

			jobDto.setLastEndTime(new Date());
			jobDto.setActive(false);
			jobDto.setNextStartTime(trigger.nextExecutionTime(jobDto));
			LOG.info("Completed Scheduled Job {} for Plugin {}", job.getJobName(), pluginDisplayName);
		}, trigger);
	}

	/**
	 * Process key rotations using the defined schedule
	 *
	 * @param job the scheduler job configuration to use
	 */
	public void scheduleKeyRotationJob(SchedulerJob job) {
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
		this.scheduler.schedule(() -> {
			LOG.info("Beginning Scheduled Job {}", job.getJobName());
			job.getJob().run();
			LOG.info("Completed Scheduled Job {}", job.getJobName());
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
