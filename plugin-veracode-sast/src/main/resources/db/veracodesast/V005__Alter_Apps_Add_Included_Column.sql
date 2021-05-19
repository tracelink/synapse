ALTER TABLE veracode_sast_apps ADD COLUMN included BOOLEAN;
UPDATE veracode_sast_apps SET included = TRUE;
ALTER TABLE veracode_sast_apps ALTER COLUMN included SET NOT NULL;
