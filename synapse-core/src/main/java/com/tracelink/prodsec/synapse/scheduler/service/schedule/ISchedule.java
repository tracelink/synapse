package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import org.springframework.scheduling.Trigger;

/**
 * A schedule object that configures the schedule on which a job should run
 *
 * @author csmith
 */
public interface ISchedule {

	/**
	 * Make or get the trigger for this schedule
	 *
	 * @return the trigger for this job schedule
	 */
	Trigger makeTrigger();
}
