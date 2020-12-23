package com.tracelink.prodsec.plugin.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.tracelink.prodsec.plugin.demo.service.DemoService;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.PluginWithDatabase;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePluginDatabaseEnabled;

/**
 * The primary class to load the {@link Plugin} configuration. This classname is
 * added to the /src/main/resources/META-INF/spring.factories file.
 * 
 * This implements the {@link PluginWithDatabase} using both the Annotation (to
 * load spring beans) and the extension (to connect with the plugin loader).
 * 
 * The Demo Plugin itself is a simple, self-contained implementation allowing
 * privileged users to manually add the total number of vulnerabilities found in
 * an project to the project itself. Then, any user can see the total
 * vulnerabilities in the scorecard.
 * 
 * The plugin demonstrates how to link data structures to the main Synapse
 * objects in code and in the database.
 * 
 * 
 * @author csmith
 *
 */
@SynapsePluginDatabaseEnabled
public class DemoPlugin extends PluginWithDatabase {

	/**
	 * This is made public to share the schema definition with the model
	 */
	public static final String SCHEMA = "demo";

	/**
	 * This is made public to share the privilege definition with the controller
	 */
	public static final String PRIV = "demoAdmin";

	/**
	 * This is made public to share the pagelink definition with the controller
	 */
	public static final String PAGELINK = "/demo";

	private final DemoService demoService;

	/**
	 * Create the plugin with the pre-configured DemoService
	 * 
	 * @param demoService the service for this demo plugin
	 */
	public DemoPlugin(@Autowired DemoService demoService) {
		this.demoService = demoService;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	@Override
	protected String getMigrationsLocation() {
		return "db/demo/";
	}

	@Override
	protected PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("Demo Plugin", "stars");
	}

	@Override
	protected List<SchedulerJob> getJobsForScheduler() {
		return Arrays.asList(
				// Create the configuration for a regular job
				new SimpleSchedulerJob("Demo Job").withJob(demoService::logVulns)
						.onSchedule(new PeriodicSchedule(10, TimeUnit.SECONDS)));
	}

	@Override
	protected List<ScorecardColumn> getColumnsForScorecard() {
		return Arrays.asList(
				// Create the scorecard column for reporting
				new SimpleScorecardColumn("Demo Column").withPageLink(PAGELINK)
						.withProjectCallback(demoService::projectCallback)
						.withProductLineCallback(demoService::productLineCallback));
	}

	@Override
	protected List<SidebarLink> getLinksForSidebar() {
		return Arrays.asList(
				// Create the configuration locked to a specific priv
				new SimpleSidebarLink("Configure Demo").withPrivileges(PRIV).withMaterialIcon("swap_vert")
						.withPageLink(PAGELINK));
	}

	@Override
	protected List<String> getPrivileges() {
		return Arrays.asList(PRIV);
	}

}
