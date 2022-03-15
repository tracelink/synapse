package com.tracelink.prodsec.synapse.spi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.logging.service.PluginLogger;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.spi.model.PluginModel;
import com.tracelink.prodsec.synapse.spi.repository.PluginRepository;

/**
 * Service used to register plugins, and activate/deactivate plugins
 * 
 * @author csmith
 *
 */
@Service
public class PluginService {
	private static final Logger LOG = LoggerFactory.getLogger(PluginService.class);

	private LoggingService logsService;

	private ScorecardService scorecardService;

	private SidebarService sidebar;

	private SchedulerService scheduler;

	private AuthService auth;

	private final PluginRepository pluginRepository;

	private final Map<String, Plugin> pluginMap;

	public PluginService(@Autowired PluginRepository pluginRepository, @Autowired LoggingService logsService,
			@Autowired ScorecardService scorecardService, @Autowired SidebarService sidebar,
			@Autowired SchedulerService scheduler, @Autowired AuthService auth) {
		this.pluginRepository = pluginRepository;
		this.logsService = logsService;
		this.scorecardService = scorecardService;
		this.sidebar = sidebar;
		this.scheduler = scheduler;
		this.auth = auth;
		this.pluginMap = new HashMap<>();
	}

	public PluginModel getPlugin(String pluginName) {
		return pluginRepository.getByPluginName(pluginName);
	}

	public PluginModel getPlugin(long pluginId) {
		return pluginRepository.getById(pluginId);
	}

	/**
	 * enables a plugin to be used by Synapse. This will set the entity's activation
	 * to true while also registering the plugin with all internal services
	 * 
	 * @param pluginModel the plugin to activate
	 * @return the plugin in its activated state
	 */
	public PluginModel activate(PluginModel pluginModel) {
		pluginModel.setActivated(true);
		Plugin plugin = pluginMap.get(pluginModel.getPluginName());
		if (plugin == null) {
			LOG.error(
					"Attempted to activate a plugin " + pluginModel.getPluginName() + " that has not been registered");
			throw new IllegalArgumentException("Plugin is unknown");
		}
		LOG.info("Registering Logging : " + pluginModel.getPluginName());
		registerWithLogging(plugin);

		LOG.info("Registering Auth: " + pluginModel.getPluginName());
		registerWithAuth(plugin);

		LOG.info("Registering Scorebard: " + pluginModel.getPluginName());
		registerWithScorecard(plugin);

		LOG.info("Registering Sidebar: " + pluginModel.getPluginName());
		registerWithSidebar(plugin);

		LOG.info("Registering Scheduler: " + pluginModel.getPluginName());
		registerWithScheduler(plugin);

		return save(pluginModel);
	}

	/**
	 * disables a plugin to be used by Synapse. This will set the entity's
	 * activation to false while also unregistering the plugin with all internal
	 * services
	 * 
	 * @param pluginModel the plugin to deactivate
	 * @return the plugin in its deactivated state
	 */
	public PluginModel deactivate(PluginModel pluginModel) {
		pluginModel.setActivated(false);
		Plugin plugin = pluginMap.get(pluginModel.getPluginName());
		if (plugin == null) {
			LOG.error("Attempted to deactivate a plugin " + pluginModel.getPluginName()
					+ " that has not been registered");
			throw new IllegalArgumentException("Plugin is unknown");
		}
		LOG.info("Unregistering Logging: " + pluginModel.getPluginName());
		unregisterWithLogging(plugin);

		LOG.info("Unregistering Auth: " + pluginModel.getPluginName());
		unregisterWithAuth(plugin);

		LOG.info("Unregistering Scorebard: " + pluginModel.getPluginName());
		unregisterWithScorecard(plugin);

		LOG.info("Unregistering Sidebar: " + pluginModel.getPluginName());
		unregisterWithSidebar(plugin);

		LOG.info("Unregistering Scheduler: " + pluginModel.getPluginName());
		unregisterWithScheduler(plugin);

		return save(pluginModel);
	}

	private PluginModel save(PluginModel plugin) {
		return pluginRepository.saveAndFlush(plugin);
	}

	private PluginModel createNew(String pluginName) {
		PluginModel plugin = new PluginModel();
		plugin.setPluginName(pluginName);
		plugin.setActivated(false);
		return save(plugin);
	}

	private void registerModel(Plugin plugin) {
		PluginModel model = getPlugin(plugin.getPluginDisplayGroup().getDisplayName());
		if (model == null) {
			model = createNew(plugin.getPluginDisplayGroup().getDisplayName());
		}
		this.pluginMap.put(model.getPluginName(), plugin);
		// On register, start activate/deactivate process
		if (model.isActivated()) {
			activate(model);
		}
	}

	private void registerWithLogging(Plugin plugin) {
		logsService.registerLogger(new PluginLogger(plugin.getPluginDisplayGroup().getDisplayName(),
				this.getClass().getPackage().getName()));
	}

	private void unregisterWithLogging(Plugin plugin) {
		logsService.unregisterLogger(plugin.getPluginDisplayGroup().getDisplayName());
	}

	private void registerWithScheduler(Plugin plugin) {
		List<SchedulerJob> jobs = plugin.getJobsForScheduler();
		if (jobs != null) {
			jobs.forEach(job -> this.scheduler.scheduleJob(plugin.getPluginDisplayGroup().getDisplayName(), job));
		}
	}

	private void unregisterWithScheduler(Plugin plugin) {
		List<SchedulerJob> jobs = plugin.getJobsForScheduler();
		if (jobs != null) {
			jobs.forEach(job -> this.scheduler.unscheduleJob(plugin.getPluginDisplayGroup().getDisplayName(), job));
		}
	}

	private void registerWithScorecard(Plugin plugin) {
		List<ScorecardColumn> columns = plugin.getColumnsForScorecard();
		if (columns != null) {
			columns.forEach(column -> this.scorecardService.addColumn(column));
		}
	}

	private void unregisterWithScorecard(Plugin plugin) {
		List<ScorecardColumn> columns = plugin.getColumnsForScorecard();
		if (columns != null) {
			columns.forEach(column -> this.scorecardService.removeColumn(column.getColumnName()));
		}
	}

	private void registerWithSidebar(Plugin plugin) {
		List<SidebarLink> links = plugin.getLinksForSidebar();
		if (links != null) {
			links.forEach(link -> this.sidebar.addLink(plugin.getPluginDisplayGroup(), link));
		}
	}

	private void unregisterWithSidebar(Plugin plugin) {
		this.sidebar.removeLinks(plugin.getPluginDisplayGroup());
	}

	private void registerWithAuth(Plugin plugin) {
		List<String> privs = plugin.getPrivileges();
		if (privs != null) {
			privs.forEach(priv -> this.auth.createOrGetPrivilege(priv));
		}
	}

	private void unregisterWithAuth(Plugin plugin) {
		List<String> privs = plugin.getPrivileges();
		if (privs != null) {
			privs.forEach(priv -> this.auth.removePrivilege(priv));
		}
	}

	/**
	 * Register a new Plugin with the service. If this plugin is new to Synapse, it
	 * will start deactivated, if it is previously known to Synapse, its state will
	 * be restored
	 * 
	 * @param plugin the plugin to register
	 */
	public void registerPlugin(Plugin plugin) {
		String name = null;
		if (plugin.getPluginDisplayGroup() != null) {
			name = plugin.getPluginDisplayGroup().getDisplayName();
		}
		if (!StringUtils.hasLength(name)) {
			throw new IllegalStateException("Plugin " + getClass().toString()
					+ " could not be created as it does not contain a name. Please implement the PluginDisplayGroup");
		}
		LOG.info("Register Plugin: " + name);
		registerModel(plugin);
	}

	public List<PluginModel> getPlugins() {
		return pluginRepository.findAll(Sort.by(Direction.ASC, "pluginName"));
	}

}
