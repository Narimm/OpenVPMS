# Migration script to fix the primary key of the
# entity_classifications, contact_classifications and
# product_price_classifications table as per OBF-153
#
# This is only applicable when migrating a 1.0-alpha-1 database to 1.0-beta-1
# using the original version of the migrate-1.0-alpha1-to-beta1.sql script
# which does not change the primary keys of these tables.
#


# drop primary key of *classifications tables
alter table entity_classifications drop primary key;

alter table contact_classifications drop primary key;

alter table product_price_classifications drop primary key;

# add primary keys
alter table entity_classifications add primary key (entity_id, lookup_id);

alter table contact_classifications add primary key (contact_id, lookup_id);

alter table product_price_classifications add primary key (product_price_id, lookup_id);