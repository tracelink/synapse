package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

public class PeriodicScheduleTest {
	@Test
	public void testDAO() {
		long period = 1L;
		TimeUnit time = TimeUnit.MILLISECONDS;
		PeriodicSchedule schedule = new PeriodicSchedule(period, time);
		Trigger trigger = schedule.makeTrigger();
		Assert.assertEquals(PeriodicTrigger.class, trigger.getClass());
		Assert.assertEquals(period, ((PeriodicTrigger) trigger).getPeriod());
		Assert.assertEquals(time, ((PeriodicTrigger) trigger).getTimeUnit());
	}
}
