#
# Script to modify the database schema from 1.0-beta-1 to 1.0-beta-2
#

# add the allocated_amount column to financial_acts
alter table financial_acts
add column allocated_amount numeric(19,2);

update financial_acts
set allocated_amount = 0
where allocated_amount is null;

# add index on participation concept
create index participation_arch_concept_idx on participations (arch_concept);

# change text columns to varchar

alter table assertion_descriptors modify property_map varchar(20000);

# add delete cascade constraints

alter table parties drop foreign key FKD0BCCA04D7D260F7;
alter table parties
   add constraint FKD0BCCA04D7D260F7 foreign key (party_id)
   references entities (entity_id) on delete cascade;

alter table entity_identities drop foreign key FKB1D93FB864CA9ADA;
alter table entity_identities
   add constraint FKB1D93FB864CA9ADA foreign key (entity_id)
   references entities (entity_id) on delete cascade;

alter table contacts drop foreign key FKDE2D605349856C8F;
alter table contacts
   add constraint FKDE2D605349856C8F foreign key (party_id)
   references parties (party_id) on delete cascade;

alter table product_prices drop foreign key FKFBD40D9A43C96078;
alter table product_prices
   add constraint FKFBD40D9A43C96078 foreign key (product_id)
   references products (product_id) on delete cascade;

alter table products drop foreign key FKC42BD164F96B864E;
alter table products
   add constraint FKC42BD164F96B864E foreign key (product_id)
   references entities (entity_id) on delete cascade;

alter table users drop foreign key FK6A68E088829A9F2;
alter table users
   add constraint FK6A68E088829A9F2 foreign key (user_id)
   references entities (entity_id) on delete cascade;

alter table financial_acts drop foreign key FK3D69E4178C4205B1;
alter table financial_acts
   add constraint FK3D69E4178C4205B1 foreign key (financial_act_id)
   references acts (act_id) on delete cascade;

alter table document_acts drop foreign key FK5E78FAC5A5E628DF;
alter table document_acts
   add constraint FK5E78FAC5A5E628DF foreign key (document_act_id)
   references acts (act_id) on delete cascade;

alter table node_descriptors drop foreign key FKAB46B4278E2DBBF8;
alter table node_descriptors
   add constraint FKAB46B4278E2DBBF8 foreign key (parent_id)
   references node_descriptors (node_desc_id) on delete cascade;

alter table node_descriptors drop foreign key FKAB46B42741D3C6F4;
alter table node_descriptors
   add constraint FKAB46B42741D3C6F4 foreign key (archetype_desc_id)
   references archetype_descriptors (archetype_desc_id) on delete cascade;

# create details tables

create table act_details (
    act_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (act_id, name));

alter table act_details
    add index FKFB795F95ACF0613B (act_id),
    add constraint FKFB795F95ACF0613B foreign key (act_id)
    references acts (act_id);

create table act_relationship_details (
    act_relationship_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (act_relationship_id, name));

alter table act_relationship_details
    add index FKFF1068C841148A00 (act_relationship_id),
    add constraint FKFF1068C841148A00 foreign key (act_relationship_id)
    references act_relationships (act_relationship_id);

create table contact_details (
    contact_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (contact_id, name));

alter table contact_details
    add index FKA3499D23519E590F (contact_id),
    add constraint FKA3499D23519E590F foreign key (contact_id)
    references contacts (contact_id);

create table document_details (
    document_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (document_id, name));

alter table document_details
    add index FK829C1F1E423E836A (document_id),
    add constraint FK829C1F1E423E836A foreign key (document_id)
    references documents (document_id);

create table entity_details (
    entity_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (entity_id, name));

alter table entity_details
    add index FKD621E9E664CA9ADA (entity_id),
    add constraint FKD621E9E664CA9ADA foreign key (entity_id)
    references entities (entity_id);

create table entity_identity_details (
    entity_identity_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (entity_identity_id, name));

alter table entity_identity_details
    add index FK4794CC9DBDCAE3A1 (entity_identity_id),
    add constraint FK4794CC9DBDCAE3A1 foreign key (entity_identity_id)
    references entity_identities (entity_identity_id);

create table entity_relationship_details (
    entity_relationship_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (entity_relationship_id, name));

alter table entity_relationship_details
    add index FKBB44EA1739208DE1 (entity_relationship_id),
    add constraint FKBB44EA1739208DE1 foreign key (entity_relationship_id)
    references entity_relationships (entity_relationship_id);

create table lookup_details (
    lookup_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (lookup_id, name));

alter table lookup_details
    add index FKB2E8287DCEABD10B (lookup_id),
    add constraint FKB2E8287DCEABD10B foreign key (lookup_id)
    references lookups (lookup_id);

create table lookup_relationship_details (
    lookup_relationship_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (lookup_relationship_id, name));

alter table lookup_relationship_details
    add index FK40E558E0BAD140A0 (lookup_relationship_id),
    add constraint FK40E558E0BAD140A0 foreign key (lookup_relationship_id)
    references lookup_relationships (lookup_relationship_id);

create table participation_details (
    participation_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (participation_id, name));

alter table participation_details
    add index FK64ED55445A24061A (participation_id),
    add constraint FK64ED55445A24061A foreign key (participation_id)
    references participations (participation_id);

create table product_price_details (
    product_price_id bigint not null,
    type varchar(255),
    value varchar(5000),
    name varchar(255) not null,
    primary key (product_price_id, name));

alter table product_price_details
    add index FKF9A9C1FCBCF3BC5D (product_price_id),
    add constraint FKF9A9C1FCBCF3BC5D foreign key (product_price_id)
    references product_prices (product_price_id);
