package com.tracelink.prodsec.plugin.sme;

import com.tracelink.prodsec.plugin.sme.service.SMEService;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.test.PluginDBTestBuilder;
import com.tracelink.prodsec.synapse.test.PluginDBTestHarness;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SMEPluginTest extends PluginDBTestHarness {

	@MockBean
	private SMEService mockSmeService;

	@Override
	protected Plugin buildPlugin() {
		return new SMEPlugin(mockSmeService);
	}

	@Override
	protected void configurePluginDBTester(PluginDBTestBuilder<?> testPlan) {
		testPlan.withDisplayGroup("Subject Matter Experts", "supervisor_account")
				.withScorecardColumn("Subject Matter Experts", "/sme", true, true)
				.withSidebarLink("SME List", null, "/sme", "mood").withMigrationLocation("db/sme/")
				.withSchemaName("sme");
	}

}
