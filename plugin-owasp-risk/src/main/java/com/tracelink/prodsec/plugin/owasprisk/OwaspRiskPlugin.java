package com.tracelink.prodsec.plugin.owasprisk;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.tracelink.prodsec.synapse.mvc.SynapsePublicRequestMatcherService;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePlugin;

/**
 * A plugin to expose a Risk Rating Methodology that allows both public and
 * authenticated access. There's a single page that handles everything
 * client-side
 * 
 * @author csmith
 *
 */
@SynapsePlugin
public class OwaspRiskPlugin extends Plugin {

	public OwaspRiskPlugin(@Autowired SynapsePublicRequestMatcherService matcherService) {
		matcherService.registerMatcher(new AntPathRequestMatcher("/risk_rating"));
	}

	@Override
	protected PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("OWASP Risk Rating", "rate_review");
	}

	@Override
	protected List<SidebarLink> getLinksForSidebar() {
		return Arrays
				.asList(new SimpleSidebarLink("Risk Rating").withMaterialIcon("gavel").withPageLink("/risk_rating"));
	}

	@Override
	protected List<SchedulerJob> getJobsForScheduler() {
		return null;
	}

	@Override
	protected List<ScorecardColumn> getColumnsForScorecard() {
		return null;
	}

	@Override
	protected List<String> getPrivileges() {
		return null;
	}

}
