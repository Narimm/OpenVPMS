Generating the schema
---------------------

The OpenVPMS schema is located in src/main/resources/org/openvpms/db/schema/schema.sql
This is currently generated manually by:

1. dropping the existing schema, and re-creating it in mysql:
   drop schema openvpms_dev;
   create schema openvpms_dev;

2. Running hibernate3:hbm2ddl in openvpms-release
   This is required as it includes the etl_log table from openvpms-etl-load

   > cd ../openvpms-release
   > mvn hibernate3:hbm2ddl
   > cd ../openvpms-db
   >  mysqldump --no-data -u openvpms -p openvpms_dev >| src/main/resources/org/openvpms/db/schema/schema.sql

3. Comparing the new schema with the previous version, to ensure the correct tables have been included.
   NOTE: mysqldump doesn't guarantee order of constraints. Any swapping of constraints should be reverted to avoid
   unnecessary changes.