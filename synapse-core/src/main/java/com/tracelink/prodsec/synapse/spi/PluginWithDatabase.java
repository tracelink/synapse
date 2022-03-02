package com.tracelink.prodsec.synapse.spi;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracelink.prodsec.synapse.spi.annotation.SynapsePluginDatabaseEnabled;

/**
 * The PluginWithDatabase is an extention for Synapse Plugins to enable Database
 * functionality.
 * 
 * It adds functionality to the {@link Plugin} to provide database migration and
 * schema functionality. It also does not provide any Spring controls. See
 * {@link SynapsePluginDatabaseEnabled} for that functionality, including JPA
 * and Entity creation
 * 
 * @author csmith
 *
 */
@Component
public abstract class PluginWithDatabase extends Plugin {
	private static final Logger LOG = LoggerFactory.getLogger(PluginWithDatabase.class);

	@Autowired
	private Flyway flyway;

	@Override
	protected void buildPlugin() {
		LOG.info("BUILDING DB PLUGIN: " + getPluginDisplayGroup().getDisplayName());

		ClassicConfiguration compConfig = new ClassicConfiguration(flyway.getConfiguration());

		// set the correct schema name
		compConfig.setSchemas(new String[] {getSchemaName()});

		// set the correct location
		compConfig.setLocations(new Location(getMigrationsLocation()));

		// migrate
		Flyway.configure().configuration(compConfig).load().migrate();
		super.buildPlugin();
	}

	/**
	 * The DB Schema Name.
	 * 
	 * In general, it is a good practice to have the schema named after the plugin
	 * to ensure it is unique. Multiple plugins *could* share a schema, but the
	 * migrations would not operate well and would likely throw exceptions
	 * 
	 * @return a schema for this plugin.
	 */
	protected abstract String getSchemaName();

	/**
	 * The Flyway migration scripts folder.
	 * 
	 * In general, it is a good practice to have the migration location be a folder
	 * in the project named after the plugin. During multiple plugin loads,
	 * depending on the ordering of the loads, it is possible to have one plugin
	 * find the migration location of another plugin and the app will not start
	 * correctly
	 * 
	 * e.g. "/db/demo" to get the folder
	 * /src/main/resources/db/demo/{V001__[name].sql, V002__[name]...}
	 * 
	 * @return the location of the migrations folder in the resources directory
	 */
	protected abstract String getMigrationsLocation();

}
