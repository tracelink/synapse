ALTER TABLE veracode_sca_workspaces RENAME COLUMN excluded TO included;
UPDATE veracode_sca_workspaces SET included = NOT included;
