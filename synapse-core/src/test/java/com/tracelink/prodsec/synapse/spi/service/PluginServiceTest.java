package com.tracelink.prodsec.synapse.spi.service;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.model.PluginModel;
import com.tracelink.prodsec.synapse.spi.repository.PluginRepository;

@RunWith(SpringRunner.class)
public class PluginServiceTest {
	@MockBean
	private PluginRepository mockPluginRepository;

	@MockBean
	private LoggingService mockLogsService;

	@MockBean
	private ScorecardService mockScorecardService;

	@MockBean
	private SidebarService mockSidebar;

	@MockBean
	private SchedulerService mockScheduler;

	@MockBean
	private AuthService mockAuth;

	private PluginService pluginService;

	@Before
	public void setup() {
		pluginService = new PluginService(mockPluginRepository, mockLogsService, mockScorecardService, mockSidebar,
				mockScheduler, mockAuth);
	}

	@Test
	public void testGetPlugin() {
		pluginService.getPlugin("foo");
		BDDMockito.verify(mockPluginRepository).getByPluginName("foo");
	}

	@Test
	public void testGetPluginId() {
		pluginService.getPlugin(1L);
		BDDMockito.verify(mockPluginRepository).getById(1L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testActivateFail() {
		PluginModel pm = new PluginModel();
		pm.setPluginName("foo");
		pluginService.activate(pm);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeactivateFail() {
		PluginModel pm = new PluginModel();
		pm.setPluginName("foo");
		pluginService.deactivate(pm);
	}

	@Test
	public void testRegisterPluginNew() {
		String name = "foo";
		BDDMockito.when(mockPluginRepository.saveAndFlush(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));
		Plugin plugin = BDDMockito.mock(Plugin.class);
		BDDMockito.when(plugin.getPluginDisplayGroup()).thenReturn(new PluginDisplayGroup(name, "bar"));

		pluginService.registerPlugin(plugin);

		ArgumentCaptor<PluginModel> pmCaptor = ArgumentCaptor.forClass(PluginModel.class);
		BDDMockito.verify(mockPluginRepository).saveAndFlush(pmCaptor.capture());
		Assert.assertEquals(pmCaptor.getValue().getPluginName(), name);
	}

	@Test
	public void testRegisterPluginPreviousActive() {
		String name = "foo";
		Plugin plugin = BDDMockito.mock(Plugin.class);

		BDDMockito.when(plugin.getPluginDisplayGroup()).thenReturn(new PluginDisplayGroup(name, null));
		BDDMockito.when(plugin.getPrivileges()).thenReturn(Arrays.asList("priv"));
		BDDMockito.when(plugin.getColumnsForScorecard()).thenReturn(Arrays.asList(new SimpleScorecardColumn("column")));
		BDDMockito.when(plugin.getLinksForSidebar()).thenReturn(Arrays.asList(new SimpleSidebarLink("sidebar")));
		BDDMockito.when(plugin.getJobsForScheduler()).thenReturn(Arrays.asList(new SimpleSchedulerJob("job")));

		PluginModel model = new PluginModel();
		model.setActivated(true);
		model.setPluginName(name);
		BDDMockito.when(mockPluginRepository.getByPluginName(BDDMockito.anyString())).thenReturn(model);

		pluginService.registerPlugin(plugin);
		BDDMockito.verify(mockLogsService).registerLogger(BDDMockito.any());
		BDDMockito.verify(mockAuth).createOrGetPrivilege(BDDMockito.anyString());
		BDDMockito.verify(mockScorecardService).addColumn(BDDMockito.any());
		BDDMockito.verify(mockSidebar).addLink(BDDMockito.any(), BDDMockito.any());
		BDDMockito.verify(mockScheduler).scheduleJob(BDDMockito.anyString(), BDDMockito.any());
	}

	@Test
	public void testRegisterPluginPreviousNotActive() {
		String name = "foo";
		Plugin plugin = BDDMockito.mock(Plugin.class);

		BDDMockito.when(plugin.getPluginDisplayGroup()).thenReturn(new PluginDisplayGroup(name, null));
		BDDMockito.when(plugin.getPrivileges()).thenReturn(Arrays.asList("priv"));
		BDDMockito.when(plugin.getColumnsForScorecard()).thenReturn(Arrays.asList(new SimpleScorecardColumn("column")));
		BDDMockito.when(plugin.getLinksForSidebar()).thenReturn(Arrays.asList(new SimpleSidebarLink("sidebar")));
		BDDMockito.when(plugin.getJobsForScheduler()).thenReturn(Arrays.asList(new SimpleSchedulerJob("job")));

		PluginModel model = new PluginModel();
		model.setActivated(false);
		model.setPluginName(name);
		BDDMockito.when(mockPluginRepository.getByPluginName(BDDMockito.anyString())).thenReturn(model);

		pluginService.registerPlugin(plugin);
		BDDMockito.verify(mockLogsService, BDDMockito.times(0)).registerLogger(BDDMockito.any());
		BDDMockito.verify(mockAuth, BDDMockito.times(0)).createOrGetPrivilege(BDDMockito.anyString());
		BDDMockito.verify(mockScorecardService, BDDMockito.times(0)).addColumn(BDDMockito.any());
		BDDMockito.verify(mockSidebar, BDDMockito.times(0)).addLink(BDDMockito.any(), BDDMockito.any());
		BDDMockito.verify(mockScheduler, BDDMockito.times(0)).scheduleJob(BDDMockito.anyString(), BDDMockito.any());
	}

	@Test
	public void testDeactivatePlugin() {
		String name = "foo";
		Plugin plugin = BDDMockito.mock(Plugin.class);

		BDDMockito.when(plugin.getPluginDisplayGroup()).thenReturn(new PluginDisplayGroup(name, null));
		BDDMockito.when(plugin.getPrivileges()).thenReturn(Arrays.asList("priv"));
		BDDMockito.when(plugin.getColumnsForScorecard()).thenReturn(Arrays.asList(new SimpleScorecardColumn("column")));
		BDDMockito.when(plugin.getLinksForSidebar()).thenReturn(Arrays.asList(new SimpleSidebarLink("sidebar")));
		BDDMockito.when(plugin.getJobsForScheduler()).thenReturn(Arrays.asList(new SimpleSchedulerJob("job")));

		PluginModel model = new PluginModel();
		model.setActivated(false);
		model.setPluginName(name);
		BDDMockito.when(mockPluginRepository.getByPluginName(BDDMockito.anyString())).thenReturn(model);

		pluginService.registerPlugin(plugin);
		pluginService.deactivate(model);
		BDDMockito.verify(mockLogsService).unregisterLogger(BDDMockito.anyString());
		BDDMockito.verify(mockAuth).removePrivilege(BDDMockito.anyString());
		BDDMockito.verify(mockScorecardService).removeColumn(BDDMockito.anyString());
		BDDMockito.verify(mockSidebar).removeLinks(BDDMockito.any());
		BDDMockito.verify(mockScheduler).unscheduleJob(BDDMockito.anyString(), BDDMockito.any());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testRegisterPluginNoName() {
		Plugin plugin = BDDMockito.mock(Plugin.class);
		BDDMockito.when(plugin.getPluginDisplayGroup()).thenReturn(new PluginDisplayGroup(null, null));
		pluginService.registerPlugin(plugin);
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisterPluginNoGroup() {
		Plugin plugin = BDDMockito.mock(Plugin.class);
		pluginService.registerPlugin(plugin);
	}

	@Test
	public void testGetPlugins() {
		pluginService.getPlugins();
		BDDMockito.verify(mockPluginRepository).findAll(BDDMockito.any(Sort.class));
	}

}
