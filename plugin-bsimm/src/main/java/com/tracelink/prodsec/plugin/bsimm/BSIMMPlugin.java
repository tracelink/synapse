package com.tracelink.prodsec.plugin.bsimm;

import com.tracelink.prodsec.plugin.bsimm.service.BsimmResponseService;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.PluginWithDatabase;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePluginDatabaseEnabled;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The BSIMM Plugin allows for uploading BSIMM surveys, completing them per
 * ProductLine, reviewing the results, and showing a Radar graph showing the
 * results
 *
 * @author csmith
 */
@SynapsePluginDatabaseEnabled
public class BSIMMPlugin extends PluginWithDatabase {

	public static final String SCHEMA = "bsimm";
	public static final String PAGELINK = "/bsimm";
	public static final String PRIV = "BSIMMResponder";

	private final BsimmResponseService bsimmService;

	public BSIMMPlugin(@Autowired BsimmResponseService bsimmService) {
		this.bsimmService = bsimmService;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	@Override
	protected String getMigrationsLocation() {
		return "db/bsimm";
	}

	@Override
	public PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("BSIMM", "timeline");
	}

	@Override
	public List<ScorecardColumn> getColumnsForScorecard() {
		return Arrays.asList(new SimpleScorecardColumn("BSIMM Score").withPageLink(PAGELINK)
				.withProductLineCallback(bsimmService::getProductLineScorecard));
	}

	@Override
	public List<SidebarLink> getLinksForSidebar() {
		return Arrays.asList(new SimpleSidebarLink("Overview").withMaterialIcon("dashboard")
						.withPageLink(PAGELINK),
				new SimpleSidebarLink("Surveys").withMaterialIcon("assignment")
						.withPageLink(PAGELINK + "/survey"));
	}

	@Override
	public List<String> getPrivileges() {
		return Arrays.asList(PRIV);
	}

	/**
	 * Unimplemented
	 */
	@Override
	public List<SchedulerJob> getJobsForScheduler() {
		return null;
	}
}
