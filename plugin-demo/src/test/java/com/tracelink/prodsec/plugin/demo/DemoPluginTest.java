package com.tracelink.prodsec.plugin.demo;

import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.demo.service.DemoService;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.test.PluginDBTestBuilder;
import com.tracelink.prodsec.synapse.test.PluginDBTestHarness;

@RunWith(SpringRunner.class)
public class DemoPluginTest extends PluginDBTestHarness {

	@MockBean
	private DemoService mockDemoService;

	@Override
	protected Plugin buildPlugin() {
		return new DemoPlugin(mockDemoService);
	}

	@Override
	protected void configurePluginDBTester(PluginDBTestBuilder<?> testPlan) {
		testPlan.withDisplayGroup("Demo Plugin", "stars").withScorecardColumn("Demo Column", "/demo", true, true)
				.withSidebarLink("Configure Demo", new String[] { "demoAdmin" }, "/demo", "swap_vert")
				.withJobConfiguration("Demo Job", true, new PeriodicSchedule(10, TimeUnit.SECONDS))
				.withPrivilege("demoAdmin").withMigrationLocation("db/demo/").withSchemaName("demo");
	}

}
