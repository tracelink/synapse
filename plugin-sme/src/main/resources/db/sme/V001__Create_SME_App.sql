CREATE TABLE sme_names (
  sme_id BIGSERIAL NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (sme_id)
);

CREATE TABLE sme_project (
  sme_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  PRIMARY KEY (sme_id, project_id),
  CONSTRAINT fk_sme_smeprojects_smeid FOREIGN KEY (sme_id) REFERENCES sme_names (sme_id),
  CONSTRAINT fk_sme_smeprojects_projectid FOREIGN KEY (project_id) REFERENCES synapsecore.project (project_id)
);
