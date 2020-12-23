package com.tracelink.prodsec.synapse.scheduler.service;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final ConcurrentTaskScheduler scheduler;
	private final JobsService jobsService;

	private volatile boolean isPaused = false;
	private final ReentrantLock pauseLock = new ReentrantLock();
	private final Condition unpaused = pauseLock.newCondition();

	public SchedulerService(@Autowired ConcurrentTaskScheduler scheduler,
			@Autowired JobsService jobsService) {
		this.scheduler = scheduler;
		this.jobsService = jobsService;
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
		jobsService.createJob(pluginDisplayName, job);

		ScheduledFuture<?> future = this.scheduler.schedule(() -> {
			LOG.info("Beginning Scheduled Job {} for Plugin {}", job.getJobName(),
					pluginDisplayName);
			jobsService.updateStartJobs(job, pluginDisplayName);
			job.getJob().run();
			jobsService.updateFinishJobs(job, pluginDisplayName);
			LOG.info("Completed Scheduled Job {} for Plugin {}", job.getJobName(),
					pluginDisplayName);
		}, job.getSchedule().makeTrigger());

		jobsService.storeFutureJob(job, pluginDisplayName, future);
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
