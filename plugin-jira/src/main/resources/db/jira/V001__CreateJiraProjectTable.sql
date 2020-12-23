CREATE TABLE jira_clients (
  client_id BIGSERIAL NOT NULL,
  api_url varchar(255) NOT NULL,
  username varchar(255) NOT NULL,
  authentication varchar(255) NOT NULL,
  PRIMARY KEY (client_id)
);

CREATE TABLE jira_thresholds (
  thresholds_id BIGSERIAL NOT NULL,
  green_yellow BIGINT NOT NULL,
  yellow_red BIGINT NOT NULL,
  PRIMARY KEY (thresholds_id)
);

CREATE TABLE jira_scrum_metrics (
  scrum_id BIGSERIAL NOT NULL,
  recorded_date DATE NOT NULL,
  todo BIGINT NOT NULL,
  prog BIGINT NOT NULL,
  block BIGINT NOT NULL,
  done BIGINT NOT NULL,
  PRIMARY KEY (scrum_id)
);

CREATE TABLE jira_vuln (
  issue_id BIGSERIAL NOT NULL,
  issue_key varchar(255) NOT NULL,
  sev varchar(255) NOT NULL,
  created DATE NOT NULL,
  resolved DATE,
  productline_id BIGINT,
  PRIMARY KEY (issue_id),
  CONSTRAINT fk_jira_vuln_productline_id FOREIGN KEY (productline_id) REFERENCES synapsecore.productline (productline_id)
);

CREATE TABLE jira_search_phrases (
  query_id BIGSERIAL NOT NULL,
  jql_phrase varchar NOT NULL,
  data_format varchar(255) NOT NULL,
  PRIMARY KEY (query_id)
);

CREATE TABLE jira_allowed_sla (
  sla_id BIGSERIAL NOT NULL,
  severity varchar(255) NOT NULL,
  allowed_days BIGINT,
  PRIMARY KEY (sla_id)
);