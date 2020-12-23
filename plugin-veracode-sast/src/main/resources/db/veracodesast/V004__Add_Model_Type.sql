ALTER TABLE veracode_sast_apps ADD model_type varchar(255) NOT NULL;
UPDATE veracode_sast_apps SET model_type = 'Sandbox';