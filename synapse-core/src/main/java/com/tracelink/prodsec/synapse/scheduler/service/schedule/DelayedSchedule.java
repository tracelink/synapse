package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

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
