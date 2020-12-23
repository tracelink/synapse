CREATE TABLE veracode_sca_clients (
  client_id BIGSERIAL NOT NULL,
  api_id varchar(255) NOT NULL,
  api_secret_key varchar(255) NOT NULL,
  PRIMARY KEY (client_id)
);

CREATE TABLE veracode_sca_thresholds (
  thresholds_id BIGSERIAL NOT NULL,
  green_yellow BIGINT NOT NULL,
  yellow_red BIGINT NOT NULL,
  PRIMARY KEY (thresholds_id)
);

CREATE TABLE veracode_sca_workspaces (
  workspace_id UUID NOT NULL,
  name varchar(255) NOT NULL,
  site_id varchar(255) NOT NULL,
  excluded BOOLEAN NOT NULL,
  PRIMARY KEY (workspace_id)
);

CREATE TABLE veracode_sca_projects (
  project_id UUID NOT NULL,
  branches TEXT NOT NULL,
  default_branch varchar(255) NOT NULL,
  last_scan_date TIMESTAMP NOT NULL,
  name varchar(255) NOT NULL,
  site_id varchar(255) NOT NULL,
  workspace_id UUID NOT NULL,
  synapse_project BIGINT DEFAULT NULL,
  PRIMARY KEY (project_id),
  CONSTRAINT fk_veracode_sca_workspace_project FOREIGN KEY (workspace_id) REFERENCES veracode_sca_workspaces (workspace_id),
  CONSTRAINT fk_veracode_sca_synapse_project FOREIGN KEY (synapse_project) REFERENCES synapsecore.project (project_id)
);

CREATE TABLE veracode_sca_issues (
  issue_id UUID NOT NULL,
  created_date TIMESTAMP NOT NULL,
  ignored_date TIMESTAMP,
  fixed_date TIMESTAMP,
  last_updated_date TIMESTAMP NOT NULL,
  ignored BOOLEAN NOT NULL,
  issue_status varchar(255) NOT NULL,
  issue_type varchar(255) NOT NULL,
  project_branch varchar(255) NOT NULL,
  project_id UUID NOT NULL,
  severity REAL NOT NULL,
  vulnerability varchar(255),
  vulnerable_method BOOLEAN,
  PRIMARY KEY (issue_id),
  CONSTRAINT fk_veracode_sca_issue_project FOREIGN KEY (project_id) REFERENCES veracode_sca_projects (project_id)
);
