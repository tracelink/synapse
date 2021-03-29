ALTER TABLE veracode_sca_projects ADD COLUMN included BOOLEAN;
UPDATE veracode_sca_projects SET included = TRUE;
ALTER TABLE veracode_sca_projects ALTER COLUMN included SET NOT NULL;
