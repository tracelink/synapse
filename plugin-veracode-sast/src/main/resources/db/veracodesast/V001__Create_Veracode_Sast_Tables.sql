CREATE TABLE veracode_sast_client_config (
  client_id BIGSERIAL NOT NULL,
  api_id varchar(255) NOT NULL,
  api_key varchar(255) NOT NULL,
  PRIMARY KEY (client_id)
);

CREATE TABLE veracode_sast_thresholds (
  thresholds_id BIGSERIAL NOT NULL,
  green_yellow INT NOT NULL,
  yellow_red INT NOT NULL,
  PRIMARY KEY (thresholds_id)
);

CREATE TABLE veracode_sast_apps (
  app_id BIGSERIAL NOT NULL,
  name varchar(255) DEFAULT NULL,
  synapse_project BIGINT DEFAULT NULL,
  PRIMARY KEY (app_id),
  CONSTRAINT fk_veracode_sast_synapse_project FOREIGN KEY (synapse_project) REFERENCES synapsecore.project (project_id)
);

CREATE TABLE veracode_sast_reports (
  report_id BIGSERIAL NOT NULL,
  report_date TIMESTAMP NOT NULL,
  build_id BIGINT NOT NULL,
  analysis_id BIGINT NOT NULL,
  app BIGINT NOT NULL,
  very_high_vios BIGINT NOT NULL,
  high_vios BIGINT NOT NULL,
  med_vios BIGINT NOT NULL,
  low_vios BIGINT NOT NULL,
  very_low_vios BIGINT NOT NULL,
  info_vios BIGINT NOT NULL,
  policy_score BIGINT NOT NULL,
  PRIMARY KEY (report_id),
  CONSTRAINT fk_reports_app FOREIGN KEY (app) REFERENCES veracode_sast_apps (app_id)
);

CREATE TABLE veracode_sast_flaws (
  flaw_id BIGSERIAL NOT NULL,
  analysis_id BIGINT NOT NULL,
  issue_id BIGINT NOT NULL,
  category_name varchar(255) NOT NULL,
  cwe_id BIGINT NOT NULL,
  cwe_name varchar(255) NOT NULL,
  severity INT NOT NULL,
  count INT NOT NULL,
  remediation_status varchar(255) NOT NULL,
  source_file varchar(255) NOT NULL,
  line_num INT NOT NULL,
  mitigation_status varchar(255) NOT NULL,
  report BIGINT NOT NULL,
  PRIMARY KEY (flaw_id),
  CONSTRAINT fk_flaws_report FOREIGN KEY (report) REFERENCES veracode_sast_reports (report_id)
);