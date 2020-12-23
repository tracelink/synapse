CREATE TABLE jobs (
  job_id BIGSERIAL NOT NULL,
  job_name varchar(255) NOT NULL,
  start TIMESTAMP,
  finish TIMESTAMP,
  PRIMARY KEY (job_id)
);
