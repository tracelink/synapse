package com.tracelink.prodsec.synapse.spi;

import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.logging.service.PluginLogger;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePlugin;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * The Plugin is the main implementation class for Synapse Plugins.
 * <p>
 * It controls the required data that Plugin implementors need to provide in
 * order to function in Synapse. It does not provide any database controls. It
 * also does not provide any Spring controls. See {@link SynapsePlugin} for that
 * functionality
 *
 * @author csmith
 */
@Component
public abstract class Plugin {

	private static final Logger LOG = LoggerFactory.getLogger(Plugin.class);

	@Autowired
	private LoggingService logsService;

	@Autowired
	private ScorecardService scorecardService;

	@Autowired
	private SidebarService sidebar;

	@Autowired
	private SchedulerService scheduler;

	@Autowired
	private AuthService auth;

	/**
	 * Required.
	 * <p>
	 * Get a name to display in the sidebar and for logging etc.
	 *
	 * @return the display name for this plugin
	 */
	protected abstract PluginDisplayGroup getPluginDisplayGroup();

	/////////////
	// Logging
	/////////////
	private void registerWithLogging() {
		logsService.registerLogger(
				new PluginLogger(getPluginDisplayGroup().getDisplayName(),
						this.getClass().getPackage().getName()));
	}

	/////////////
	// Scheduler
	/////////////
	private void configureScheduler() {
		List<SchedulerJob> jobs = getJobsForScheduler();
		if (jobs != null) {
			jobs.forEach(job -> this.scheduler
					.scheduleJob(getPluginDisplayGroup().getDisplayName(), job));
		}
	}

	/**
	 * get a list of scheduler jobs that must be run
	 *
	 * @return a list of configurations for the scheduler or null/empty if no
	 * scheduler jobs should be added
	 */
	protected abstract List<SchedulerJob> getJobsForScheduler();

	/////////////
	// Scorecard
	/////////////
	private void registerWithScorecard() {
		List<ScorecardColumn> columns = getColumnsForScorecard();
		if (columns != null) {
			columns.forEach(column -> this.scorecardService.addColumn(column));
		}
	}

	/**
	 * get a list of columns to add to the scorecard
	 *
	 * @return a list of columns to add to the scorecard, or null/empty if no
	 * columns should be added for the scorecard
	 */
	protected abstract List<ScorecardColumn> getColumnsForScorecard();

	/////////////
	// Sidebar
	/////////////
	private void registerWithSidebar() {
		List<SidebarLink> links = getLinksForSidebar();
		PluginDisplayGroup pluginDisplayGroup = getPluginDisplayGroup();
		if (links != null) {
			links.forEach(link -> this.sidebar.addLink(pluginDisplayGroup, link));
		}
	}

	/**
	 * get a list of links to add to the sidebar
	 *
	 * @return a list of links for the sidebar, or null/empty if no links should be
	 * configured in the sidebar
	 */
	protected abstract List<SidebarLink> getLinksForSidebar();

	/////////////
	// Auth
	/////////////
	private void registerWithAuth() {
		List<String> privs = getPrivileges();
		if (privs != null) {
			privs.forEach(priv -> this.auth.createOrGetPrivilege(priv));
		}
	}

	/**
	 * get a list of links to add to the auth module
	 *
	 * @return a list of privilege names, or null/empty if no privileges should be
	 * configured
	 */
	protected abstract List<String> getPrivileges();

	@PostConstruct
	protected void buildPlugin() {
		String name = null;
		if (getPluginDisplayGroup() != null) {
			name = getPluginDisplayGroup().getDisplayName();
		}
		if (StringUtils.isEmpty(name)) {
			throw new IllegalStateException("Plugin " + getClass().toString()
					+ " could not be created as it does not contain a name. Please implement the PluginDisplayGroup");
		}
		LOG.info("BUILDING PLUGIN: " + name);

		LOG.info("Registering Logging: " + name);
		registerWithLogging();
		LOG.info("Registering Auth: " + name);
		registerWithAuth();
		LOG.info("Registering Scorebard: " + name);
		registerWithScorecard();
		LOG.info("Registering Sidebar: " + name);
		registerWithSidebar();
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		String name = null;
		if (getPluginDisplayGroup() != null) {
			name = getPluginDisplayGroup().getDisplayName();
		}
		if (StringUtils.isEmpty(name)) {
			throw new IllegalStateException("Plugin " + getClass().toString()
					+ " jobs could not be scheduled as the plugin does not contain a name. Please implement the PluginDisplayGroup");
		}
		LOG.info("Registering Scheduler: " + name);
		configureScheduler();
	}
}
