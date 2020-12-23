package com.tracelink.prodsec.plugin.owasprisk;

import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.synapse.mvc.SynapsePublicRequestMatcherService;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.test.PluginTestBuilder;
import com.tracelink.prodsec.synapse.test.PluginTestHarness;

@RunWith(SpringRunner.class)
public class OwaspRiskPluginTest extends PluginTestHarness {

	@MockBean
	private SynapsePublicRequestMatcherService mockMatcherService;

	@Override
	protected Plugin buildPlugin() {
		return new OwaspRiskPlugin(mockMatcherService);
	}

	@Override
	protected void configurePluginTester(PluginTestBuilder<?> testPlan) {
		testPlan.withDisplayGroup("OWASP Risk Rating", "rate_review").withSidebarLink("Risk Rating", null,
				"/risk_rating", "gavel");
	}

}
