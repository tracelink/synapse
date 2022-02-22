DROP TABLE veracode_dast_flaws;
DROP TABLE veracode_dast_reports;

CREATE TABLE veracode_dast_reports (
  report_id BIGSERIAL NOT NULL,
  report_date TIMESTAMP NOT NULL,
  build_id BIGINT NOT NULL,
  analysis_id BIGINT NOT NULL,
  app BIGINT NOT NULL,
  policy_score BIGINT NOT NULL,
  unmitigated BIGINT NOT NULL,
  total_flaws BIGINT NOT NULL,
  report_coordinates varchar(255) NOT NULL,
  PRIMARY KEY (report_id),
  CONSTRAINT fk_reports_app FOREIGN KEY (app) REFERENCES veracode_dast_apps (app_id)
);
