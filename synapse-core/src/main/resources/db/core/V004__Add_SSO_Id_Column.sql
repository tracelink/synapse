ALTER TABLE users RENAME COLUMN email to username;
ALTER TABLE users ADD COLUMN sso_id varchar(255) DEFAULT NULL;
