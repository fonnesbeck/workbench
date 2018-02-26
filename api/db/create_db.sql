CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE USER IF NOT EXISTS '${WORKBENCH_DB_USER}'@'%' IDENTIFIED BY '${WORKBENCH_DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${LIQUIBASE_DB_USER}'@'%' IDENTIFIED BY '${LIQUIBASE_DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${PUBLIC_DB_USER}'@'%' IDENTIFIED BY '${PUBLIC_DB_PASSWORD}';
-- In case the users already exist, change their passwords.
SET PASSWORD FOR '${WORKBENCH_DB_USER}'@'%' = '${WORKBENCH_DB_PASSWORD}';
SET PASSWORD FOR '${LIQUIBASE_DB_USER}'@'%' = '${LIQUIBASE_DB_PASSWORD}';
SET PASSWORD FOR '${PUBLIC_DB_USER}'@'%' = '${PUBLIC_DB_PASSWORD}';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE TEMPORARY TABLES ON ${DB_NAME}.* TO '${WORKBENCH_DB_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, INDEX, REFERENCES, CREATE TEMPORARY TABLES, CREATE VIEW ON ${DB_NAME}.* TO '${LIQUIBASE_DB_USER}'@'%';

# Give wildcard permission to cdr databases for workbench
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE TEMPORARY TABLES ON `cdr%`.* TO '${WORKBENCH_DB_USER}'@'%';
# Give wildcard permission to public databases for public user
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE TEMPORARY TABLES ON `public%`.* TO '${PUBLIC_DB_USER}'@'%';