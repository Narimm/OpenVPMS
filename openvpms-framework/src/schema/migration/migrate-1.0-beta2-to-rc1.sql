#
# Script to update the schema from 1.0-beta-2 to 1.0-rc1
#

# delete details with null values for OBF-161
delete from act_details where value is null;
delete from act_relationship_details where value is null;
delete from contact_details where value is null;
delete from document_details where value is null;
delete from entity_details where value is null;
delete from entity_identity_details where value is null;
delete from entity_relationship_details where value is null;
delete from lookup_details where value is null;
delete from lookup_relationship_details where value is null;
delete from participation_details where value is null;
delete from product_price_details where value is null;

# alter details tables to prevent nulls for OBF-161
alter table act_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table act_relationship_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table contact_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table document_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table entity_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table entity_relationship_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table lookup_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table lookup_relationship_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table participation_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

alter table product_price_details
    modify type varchar(255) not null,
    modify value varchar(5000) not null;

