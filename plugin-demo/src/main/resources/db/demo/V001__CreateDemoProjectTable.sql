/* 
 * Basic setup of a table. At the Constraint line, note this is the
 * second half of the setup to join this table to the Synapse Core 
 * project table. This links back to the Join Column in the 
 * DemoProjectEntity.
 */
CREATE TABLE demoproject (
	demo_id BIGSERIAL NOT NULL,
	vuln INT DEFAULT 0,
	synapse_project BIGINT DEFAULT NULL,
	PRIMARY KEY (demo_id),
	CONSTRAINT fk_demo_synapse_project FOREIGN KEY (synapse_project) REFERENCES synapsecore.project (project_id)
);