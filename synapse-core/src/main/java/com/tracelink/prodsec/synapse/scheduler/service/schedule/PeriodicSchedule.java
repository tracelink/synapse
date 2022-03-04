package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link ISchedule} that uses a Periodic schedule.
 * <p>
 * The Periodic schedule is set as the distance between the end time of the
 * previous run and the start time of the next. This means the same job will
 * never run concurrently with itself
 *
 * @author csmith
 */
public class PeriodicSchedule extends DelayedSchedule {

	public PeriodicSchedule(long period, TimeUnit timeUnit) {
		super(period, 0, timeUnit);
	}
}
