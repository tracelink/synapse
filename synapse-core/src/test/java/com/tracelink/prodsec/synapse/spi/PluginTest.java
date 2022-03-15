package com.tracelink.prodsec.synapse.spi;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.service.PluginService;

@RunWith(SpringRunner.class)
public class PluginTest {

	@MockBean
	private PluginService mockPluginService;


	private Plugin injectMocks(Plugin plugin) {
		ReflectionTestUtils.setField(plugin, "pluginService", mockPluginService);
		return plugin;
	}

	@Test
	public void testPluginRuns() {
		Plugin p = new PluginTestClass();
		injectMocks(p);
		p.buildPlugin();
		BDDMockito.verify(mockPluginService).registerPlugin(p);
	}

	//////////////
	//Test class
	//////////////
	private static class PluginTestClass extends Plugin {

		@Override
		public PluginDisplayGroup getPluginDisplayGroup() {
			return null;
		}

		@Override
		public List<SchedulerJob> getJobsForScheduler() {
			return null;
		}

		@Override
		public List<ScorecardColumn> getColumnsForScorecard() {
			return null;
		}

		@Override
		public List<SidebarLink> getLinksForSidebar() {
			return null;
		}

		@Override
		public List<String> getPrivileges() {
			return null;
		}

	}
}
