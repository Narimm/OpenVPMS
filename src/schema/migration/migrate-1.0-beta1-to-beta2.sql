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

alter table acts modify details varchar(5000);
alter table act_relationships modify details varchar(5000);
alter table assertion_descriptors modify property_map varchar(20000);
alter table contacts modify details varchar(5000);
alter table documents modify details varchar(5000);
alter table entities modify details varchar(5000);
alter table entity_identities modify details varchar(5000);
alter table entity_relationships modify details varchar(5000);
alter table lookups modify details varchar(5000);
alter table lookup_relationships modify details varchar(5000);
alter table participations modify details varchar(5000);
alter table product_prices modify details varchar(5000);

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

