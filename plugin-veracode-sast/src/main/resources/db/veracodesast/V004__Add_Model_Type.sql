ALTER TABLE veracode_sast_apps ADD model_type varchar(255);
UPDATE veracode_sast_apps SET model_type = 'Sandbox';
ALTER TABLE veracode_sast_apps ALTER COLUMN model_type SET NOT NULL;