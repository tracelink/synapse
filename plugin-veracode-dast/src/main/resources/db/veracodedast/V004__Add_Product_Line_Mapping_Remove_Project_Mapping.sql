ALTER TABLE veracode_dast_apps ADD COLUMN synapse_product_line BIGINT DEFAULT NULL;
ALTER TABLE veracode_dast_apps ADD CONSTRAINT fk_veracode_dast_synapse_product_line FOREIGN KEY (synapse_product_line) REFERENCES synapsecore.productline (productline_id);

UPDATE veracode_dast_apps
SET synapse_product_line = (SELECT owning_productline FROM synapsecore.project WHERE project_id = synapse_project);

ALTER TABLE veracode_dast_apps DROP COLUMN synapse_project;
