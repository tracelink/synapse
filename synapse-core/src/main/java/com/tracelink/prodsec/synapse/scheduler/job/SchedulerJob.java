package com.tracelink.prodsec.synapse.scheduler.job;

import com.tracelink.prodsec.synapse.scheduler.service.schedule.ISchedule;

/**
 * Interface for the Schedule Job definition
 *
 * @author csmith
 */
public interface SchedulerJob {

	/**
	 * Get the Name of this job. Used for logging
	 *
	 * @return the name of this job.
	 */
	String getJobName();

	/**
	 * Get the Job to be run on a schedule
	 *
	 * @return the Job to run
	 */
	Runnable getJob();

	/**
	 * Get the Schedule on which to run the job
	 *
	 * @return the schedule to run the job on
	 */
	ISchedule getSchedule();

}
