package com.tracelink.prodsec.plugin.sme;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tracelink.prodsec.plugin.sme.service.SMEService;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.PluginWithDatabase;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePluginDatabaseEnabled;

/**
 * The SME Plugin allows individuals to be assigned to specific projects to be
 * marked as the Subject Matter Expert for that project
 * 
 * @author csmith
 *
 */
@SynapsePluginDatabaseEnabled
public class SMEPlugin extends PluginWithDatabase {

	private final SMEService smeService;

	public static final String SCHEMA = "sme";
	public static final String PAGELINK = "/sme";

	public SMEPlugin(@Autowired SMEService smeService) {
		this.smeService = smeService;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	@Override
	protected String getMigrationsLocation() {
		return "db/sme/";
	}

	@Override
	public PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("Subject Matter Experts", "supervisor_account");
	}

	@Override
	public List<ScorecardColumn> getColumnsForScorecard() {
		return Arrays.asList(//
				new SimpleScorecardColumn("Subject Matter Experts")//
						.withPageLink(PAGELINK)//
						.withProjectCallback(smeService::scorecardCallbackProject)
						.withProductLineCallback(smeService::scorecardCallbackProduct));
	}

	@Override
	public List<SidebarLink> getLinksForSidebar() {
		return Arrays.asList(//
				new SimpleSidebarLink("SME List")//
						.withMaterialIcon("mood")//
						.withPageLink(PAGELINK));
	}

	// Not implemented

	/**
	 * Not Implemented
	 */
	@Override
	public List<String> getPrivileges() {
		return null;
	}

	/**
	 * Not Implemented
	 */
	@Override
	public List<SchedulerJob> getJobsForScheduler() {
		return null;
	}

}
