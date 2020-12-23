CREATE TABLE sonatype_clients (
  client_id BIGSERIAL NOT NULL,
  api_url varchar(255) NOT NULL,
  username varchar(255) NOT NULL,
  authentication varchar(255) NOT NULL,
  PRIMARY KEY (client_id)
);

CREATE TABLE sonatype_thresholds (
  thresholds_id BIGSERIAL NOT NULL,
  green_yellow BIGINT NOT NULL,
  yellow_red BIGINT NOT NULL,
  PRIMARY KEY (thresholds_id)
);

CREATE TABLE sonatype_apps (
  app_id varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  synapse_project BIGINT DEFAULT NULL,
  PRIMARY KEY (app_id),
  CONSTRAINT fk_sonatype_synapse_project FOREIGN KEY (synapse_project) REFERENCES synapsecore.project (project_id)
);

CREATE TABLE sonatype_metrics (
  metrics_id BIGSERIAL NOT NULL,
  recorded_date DATE NOT NULL,
  app varchar(255) NOT NULL,
  high_vios BIGINT NOT NULL,
  med_vios BIGINT NOT NULL,
  low_vios BIGINT NOT NULL,
  info_vios BIGINT NOT NULL,
  PRIMARY KEY (metrics_id),
  CONSTRAINT fk_metrics_app FOREIGN KEY (app) REFERENCES sonatype_apps (app_id)
);