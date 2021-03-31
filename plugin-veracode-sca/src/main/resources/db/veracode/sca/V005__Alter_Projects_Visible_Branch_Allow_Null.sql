ALTER TABLE veracode_sca_projects RENAME COLUMN default_branch TO visible_branch;
ALTER TABLE veracode_sca_projects ALTER COLUMN visible_branch DROP NOT NULL;
ALTER TABLE veracode_sca_projects DROP COLUMN branches;
