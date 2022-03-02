DROP TABLE veracode_sast_flaws;
DROP TABLE veracode_sast_reports;

DELETE FROM veracode_sast_apps WHERE model_type = 'Sandbox';
ALTER TABLE veracode_sast_apps DROP COLUMN model_type;

CREATE TABLE veracode_sast_reports (
  report_id BIGSERIAL NOT NULL,
  report_date TIMESTAMP NOT NULL,
  build_id BIGINT NOT NULL,
  analysis_id BIGINT NOT NULL,
  app BIGINT NOT NULL,
  policy_score BIGINT NOT NULL,
  num_very_high BIGINT NOT NULL,
  num_high BIGINT NOT NULL,
  num_medium BIGINT NOT NULL,
  num_low BIGINT NOT NULL,
  num_very_low BIGINT NOT NULL,
  num_info BIGINT NOT NULL,
  unmitigated BIGINT NOT NULL,
  total_flaws BIGINT NOT NULL,
  report_coordinates varchar(255) NOT NULL,
  PRIMARY KEY (report_id),
  CONSTRAINT fk_reports_app FOREIGN KEY (app) REFERENCES veracode_sast_apps (app_id)
);
