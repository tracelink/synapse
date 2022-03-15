package com.tracelink.prodsec.synapse.spi;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePlugin;
import com.tracelink.prodsec.synapse.spi.service.PluginService;

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

	@Autowired
	private PluginService pluginService;

	/**
	 * Required.
	 * <p>
	 * Get a name to display in the sidebar and for logging etc.
	 *
	 * @return the display name for this plugin
	 */
	public abstract PluginDisplayGroup getPluginDisplayGroup();

	/**
	 * get a list of scheduler jobs that must be run
	 *
	 * @return a list of configurations for the scheduler or null/empty if no
	 *         scheduler jobs should be added
	 */
	public abstract List<SchedulerJob> getJobsForScheduler();

	/**
	 * get a list of columns to add to the scorecard
	 *
	 * @return a list of columns to add to the scorecard, or null/empty if no
	 *         columns should be added for the scorecard
	 */
	public abstract List<ScorecardColumn> getColumnsForScorecard();

	/**
	 * get a list of links to add to the sidebar
	 *
	 * @return a list of links for the sidebar, or null/empty if no links should be
	 *         configured in the sidebar
	 */
	public abstract List<SidebarLink> getLinksForSidebar();

	/**
	 * get a list of links to add to the auth module
	 *
	 * @return a list of privilege names, or null/empty if no privileges should be
	 *         configured
	 */
	public abstract List<String> getPrivileges();

	/**
	 * Performs all actions to build and register this plugin with Synapse.
	 */
	@PostConstruct
	protected void buildPlugin() {
		pluginService.registerPlugin(this);
	}

}
