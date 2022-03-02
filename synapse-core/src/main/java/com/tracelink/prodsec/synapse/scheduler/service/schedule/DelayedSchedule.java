package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * An implementation of the {@link ISchedule} that uses a Delayed schedule.
 * <p>
 * The Delayed schedule is set as the distance between the end time of the
 * previous run and the start time of the next along with a fix delay at the
 * beginning of the first execution. This means the same job will never run
 * concurrently with itself and will not start immediately
 * 
 * @author csmith
 */
public class DelayedSchedule implements ISchedule {

	private final long period;

	private final long initialDelay;

	private final TimeUnit timeUnit;

	public DelayedSchedule(long period, long initialDelay, TimeUnit timeUnit) {
		this.period = period;
		this.initialDelay = initialDelay;
		this.timeUnit = timeUnit;
	}

	@Override
	public Trigger makeTrigger() {
		PeriodicTrigger pt = new PeriodicTrigger(period, timeUnit);
		pt.setInitialDelay(initialDelay);
		return pt;
	}
}
