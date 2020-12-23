package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

/**
 * An implementation of the {@link ISchedule} that uses the Cron system to make
 * the trigger.
 * <p>
 * Cron will work exactly on schedule, meaning if the job takes 5 minutes, but
 * the cron is set for every minute, then there will be multiple jobs running
 * simultaneously. This should be avoided as the scheduler has a fixed number of
 * threads/jobs that can run concurrently
 *
 * @author csmith
 */
public class CronSchedule implements ISchedule {

	private final String cron;

	public CronSchedule(String cron) {
		this.cron = cron;
	}

	@Override
	public Trigger makeTrigger() {
		return new CronTrigger(cron);
	}

}
