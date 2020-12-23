package com.tracelink.prodsec.synapse.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;

@RunWith(SpringRunner.class)
public class PluginWithDatabaseTest {
	@MockBean
	private ScorecardService mockScorecardService;

	@MockBean
	private SidebarService mockSidebar;

	@MockBean
	private SchedulerService mockScheduler;

	@MockBean
	private AuthService mockAuth;
	
	@MockBean
	private LoggingService mockLogsService;
	
	
	private Flyway flyway;

	private final String displayName = "displayName";
	private final String materialIcon = "materialIcon";

	private final PluginDisplayGroup pdg = new PluginDisplayGroup(displayName, materialIcon);

	/*
	 * I feel like I need to explain myself here: We are using field injection
	 * exclusively in the Plugin abstraction so to make the plugin implementation as
	 * easy as possible. No constructors with odd, internal services that every
	 * plugin needs to include, instead this autowiring happens behind the scenes at
	 * the core abstraction layer(s). So in order to test this "not best practice"
	 * we have to commit a different cardinal sin and manually inject the mocks
	 * during testing using Reflection
	 */
	private PluginWithDatabase injectMocks(PluginWithDatabase plugin) {
		ReflectionTestUtils.setField(plugin, "flyway", flyway);
		ReflectionTestUtils.setField(plugin, "logsService", mockLogsService);
		return plugin;
	}

	@Test
	public void testBuildDBPlugin() throws Exception {
		String testString = "hello";
		flyway = Flyway.configure().target("1")
				.dataSource("jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "").load();
		PluginWithDatabase plugin = injectMocks(new PluginWithDatabaseTestClass("testschema", "/testdb/"));
		plugin.buildPlugin();
		Connection conn = flyway.getConfiguration().getDataSource().getConnection();
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO testschema.test (testval) VALUES (?)");
		stmt.setString(1, testString);
		stmt.execute();
		ResultSet rs = conn.createStatement().executeQuery("SELECT testval FROM testschema.test");
		rs.next();
		String result = rs.getString("testval");
		Assert.assertEquals(testString, result);
	}

	private class PluginWithDatabaseTestClass extends PluginWithDatabase {
		private final String schema;
		private final String migrations;

		public PluginWithDatabaseTestClass(String schema, String migrations) {
			this.schema = schema;
			this.migrations = migrations;
		}

		@Override
		protected String getSchemaName() {
			return schema;
		}

		@Override
		protected String getMigrationsLocation() {
			return migrations;
		}

		// Always null below
		@Override
		protected PluginDisplayGroup getPluginDisplayGroup() {
			return pdg;
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
		protected List<SidebarLink> getLinksForSidebar() {
			return null;
		}

		@Override
		protected List<String> getPrivileges() {
			return null;
		}

	}
}
