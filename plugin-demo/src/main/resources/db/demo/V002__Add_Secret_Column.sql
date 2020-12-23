/*
 * Adding a secret column to the demo project table to demonstrate DB column encryption.
 */
ALTER TABLE demoproject ADD COLUMN secret varchar(256) DEFAULT NULL;
