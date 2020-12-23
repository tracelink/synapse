package com.tracelink.prodsec.synapse.scheduler.service.schedule;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

public class CronScheduleTest {

	@Test
	public void testDAO() {
		String cron = "0 * * * * *";
		CronSchedule schedule = new CronSchedule(cron);
		Trigger trigger = schedule.makeTrigger();
		Assert.assertEquals(CronTrigger.class, trigger.getClass());
		Assert.assertEquals(cron, ((CronTrigger) trigger).getExpression());
	}
}
