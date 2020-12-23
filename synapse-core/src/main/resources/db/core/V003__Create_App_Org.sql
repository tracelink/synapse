CREATE TABLE productline (
  productline_id BIGSERIAL NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (productline_id)
);

CREATE TABLE project (
  project_id BIGSERIAL NOT NULL,
  name varchar(255) NOT NULL,
  owning_productline BIGINT NOT NULL,
  PRIMARY KEY (project_id),
  CONSTRAINT fk_project_productline FOREIGN KEY (owning_productline) REFERENCES productline (productline_id)
);

CREATE TABLE projectfilter (
  filter_id BIGSERIAL NOT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (filter_id)
);

CREATE TABLE filter_projects (
  filter_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  PRIMARY KEY (filter_id,project_id),
  CONSTRAINT fk_projectfilter_projects_filterid FOREIGN KEY (filter_id) REFERENCES projectfilter (filter_id),
  CONSTRAINT fk_projectfilter_projects_projectid FOREIGN KEY (project_id) REFERENCES project (project_id)
);

