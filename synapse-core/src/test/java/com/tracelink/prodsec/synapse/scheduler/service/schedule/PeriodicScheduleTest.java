package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

public class PeriodicScheduleTest {
	@Test
	public void testDAO() {
		long period = 1L;
		TimeUnit time = TimeUnit.MINUTES;
		PeriodicSchedule schedule = new PeriodicSchedule(period, time);
		Trigger trigger = schedule.makeTrigger();
		Assert.assertEquals(PeriodicTrigger.class, trigger.getClass());
		Assert.assertEquals(period*60*1000, ((PeriodicTrigger) trigger).getPeriod());
		Assert.assertEquals(time, ((PeriodicTrigger) trigger).getTimeUnit());
		TriggerContext tc = new SimpleTriggerContext(new Date(), new Date(), new Date());
		Assert.assertTrue(trigger.nextExecutionTime(tc).after(new Date()));
	}
}
