package com.tracelink.prodsec.synapse.scheduler.job;

import com.tracelink.prodsec.synapse.scheduler.service.schedule.ISchedule;

/**
 * A Simple implementation of the {@link SchedulerJob} as a DAO. Uses a fluent
 * pattern
 *
 * @author csmith
 */
public class SimpleSchedulerJob implements SchedulerJob {

	private final String jobName;
	private Runnable job;
	private ISchedule schedule;

	public SimpleSchedulerJob(String jobName) {
		this.jobName = jobName;
	}

	public SimpleSchedulerJob withJob(Runnable job) {
		this.job = job;
		return this;
	}

	public SimpleSchedulerJob onSchedule(ISchedule schedule) {
		this.schedule = schedule;
		return this;
	}

	public String getJobName() {
		return this.jobName;
	}

	public Runnable getJob() {
		return this.job;
	}

	public ISchedule getSchedule() {
		return this.schedule;
	}

}
