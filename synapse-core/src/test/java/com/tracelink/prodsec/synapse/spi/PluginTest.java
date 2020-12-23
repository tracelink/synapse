package com.tracelink.prodsec.synapse.spi;

import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
public class PluginTest {

	@MockBean
	private ScorecardService mockScorecardService;

	@MockBean
	private SidebarService mockSidebar;

	@MockBean
	private SchedulerService mockScheduler;

	@MockBean
	private AuthService mockAuth;

	@MockBean
	private LoggingService mockLogsService;

	private final String displayName = "displayName";
	private final String materialIcon = "materialIcon";

	private final PluginDisplayGroup pdg = new PluginDisplayGroup(displayName, materialIcon);

	/*
	 * I feel like I need to explain myself here: We are using field injection
	 * exclusively in the Plugin abstraction so to make the plugin implementation as
	 * easy as possible. No constructors with odd, internal services that every
	 * plugin needs to include, instead this autowiring happens behind the scenes at
	 * the core abstraction layer(s). So in order to test this "not best practice"
	 * we have to commit a different cardinal sin and manually inject the mocks
	 * during testing using Reflection
	 */
	private Plugin injectMocks(Plugin plugin) {
		ReflectionTestUtils.setField(plugin, "scorecardService", mockScorecardService);
		ReflectionTestUtils.setField(plugin, "sidebar", mockSidebar);
		ReflectionTestUtils.setField(plugin, "scheduler", mockScheduler);
		ReflectionTestUtils.setField(plugin, "auth", mockAuth);
		ReflectionTestUtils.setField(plugin, "logsService", mockLogsService);
		return plugin;
	}

	@Test
	public void configureSchedulerTest() {
		SchedulerJob job = BDDMockito.mock(SchedulerJob.class);
		Plugin plugin = injectMocks(new PluginTestClass(pdg, Arrays.asList(job), null, null, null));
		ContextRefreshedEvent event = BDDMockito.mock(ContextRefreshedEvent.class);
		plugin.onApplicationEvent(event);
		BDDMockito.verify(mockScheduler).scheduleJob(displayName, job);
	}

	@Test
	public void registerWithScorecardTest() {
		ScorecardColumn col = BDDMockito.mock(ScorecardColumn.class);
		Plugin plugin = injectMocks(new PluginTestClass(pdg, null, Arrays.asList(col), null, null));
		plugin.buildPlugin();
		BDDMockito.verify(mockScorecardService).addColumn(col);
	}

	@Test
	public void registerWithSidebarTest() {
		SidebarLink link = BDDMockito.mock(SidebarLink.class);
		Plugin plugin = injectMocks(
				new PluginTestClass(pdg, null, null, Arrays.asList(link), null));
		plugin.buildPlugin();
		BDDMockito.verify(mockSidebar).addLink(pdg, link);
	}

	@Test
	public void registerWithAuthTest() {
		String priv = "priv";
		Plugin plugin = injectMocks(
				new PluginTestClass(pdg, null, null, null, Arrays.asList(priv)));
		plugin.buildPlugin();
		BDDMockito.verify(mockAuth).createOrGetPrivilege(priv);
	}

	@Test(expected = IllegalStateException.class)
	public void buildPluginNullName() {
		Plugin plugin = new PluginTestClass(null, null, null, null, null);
		plugin.buildPlugin();
	}

	//////////////
	// Test class
	//////////////
	private static class PluginTestClass extends Plugin {

		final PluginDisplayGroup pdg;
		final List<SchedulerJob> jobs;
		final List<ScorecardColumn> cols;
		final List<SidebarLink> links;
		final List<String> privs;

		public PluginTestClass(PluginDisplayGroup pdg, List<SchedulerJob> jobs,
				List<ScorecardColumn> cols,
				List<SidebarLink> links, List<String> privs) {
			this.pdg = pdg;
			this.jobs = jobs;
			this.cols = cols;
			this.links = links;
			this.privs = privs;
		}

		@Override
		protected PluginDisplayGroup getPluginDisplayGroup() {
			return pdg;
		}

		@Override
		protected List<SchedulerJob> getJobsForScheduler() {
			return jobs;
		}

		@Override
		protected List<ScorecardColumn> getColumnsForScorecard() {
			return cols;
		}

		@Override
		protected List<SidebarLink> getLinksForSidebar() {
			return links;
		}

		@Override
		protected List<String> getPrivileges() {
			return privs;
		}

	}
}
