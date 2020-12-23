package com.tracelink.prodsec.synapse.test;

@SuppressWarnings("unchecked")
public class PluginDBTestBuilder<T extends PluginDBTestBuilder<T>> extends PluginTestBuilder<PluginDBTestBuilder<T>> {
	protected String schemaName;
	protected String migration;

	public T withSchemaName(String schema) {
		this.schemaName = schema;
		return (T) this;
	}

	public T withMigrationLocation(String migration) {
		this.migration = migration;
		return (T) this;
	}
}
