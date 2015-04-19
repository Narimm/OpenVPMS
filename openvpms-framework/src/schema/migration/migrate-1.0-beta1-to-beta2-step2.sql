#
# Script to modify the database schema from 1.0-beta-1 to 1.0-beta-2
#
# Must be executed *after*:
# 1. running migrate-1.0-beta1-to-beta2-step1.sql
# 2. Migrating details columns using the DetailsMigrator tool
#

alter table acts drop column details;
alter table act_relationships drop column details;
alter table contacts drop column details;
alter table documents drop column details;
alter table entities drop column details;
alter table entity_identities drop column details;
alter table entity_relationships drop column details;
alter table lookups drop column details;
alter table lookup_relationships drop column details;
alter table participations drop column details;
alter table product_prices drop column details;