package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * An implementation of the {@link ISchedule} that uses a Periodic schedule.
 * <p>
 * The Periodic schedule is set as the distance between the end time of the
 * previous run and the start time of the next. This means the same job will
 * never run concurrently with itself
 *
 * @author csmith
 */
public class PeriodicSchedule implements ISchedule {

	private final long period;

	private final TimeUnit timeUnit;

	public PeriodicSchedule(long period, TimeUnit timeUnit) {
		this.period = period;
		this.timeUnit = timeUnit;
	}

	@Override
	public Trigger makeTrigger() {
		return new PeriodicTrigger(period, timeUnit);
	}
}
