package com.tracelink.prodsec.synapse.test;

/**
 * Builder to test database plugins.
 *
 * @param <T> the type of the builder
 */
@SuppressWarnings("unchecked")
public class PluginDBTestBuilder<T extends PluginDBTestBuilder<T>> extends
		PluginTestBuilder<PluginDBTestBuilder<T>> {

	private String schemaName;
	private String migration;

	/**
	 * Sets the DB schema name of this builder and returns this.
	 *
	 * @param schema the name of the DB schema
	 * @return this builder
	 */
	public T withSchemaName(String schema) {
		this.schemaName = schema;
		return (T) this;
	}

	/**
	 * Sets the Flyway migration location of this builder and returns this.
	 *
	 * @param migration the location of the Flyway migration scripts
	 * @return this builder
	 */
	public T withMigrationLocation(String migration) {
		this.migration = migration;
		return (T) this;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getMigration() {
		return migration;
	}
}
