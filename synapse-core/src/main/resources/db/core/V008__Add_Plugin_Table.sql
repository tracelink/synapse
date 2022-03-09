CREATE TABLE plugins (
  plugin_id BIGSERIAL NOT NULL,
  plugin_name varchar(255) NOT NULL,
  activated boolean NOT NULL,
  PRIMARY KEY (plugin_id)
);