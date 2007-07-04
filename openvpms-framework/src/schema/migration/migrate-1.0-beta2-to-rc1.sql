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

alter table entity_identity_details
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

# Data migration for OBF-162

alter table act_relationships
add column arch_short_name varchar(100) after linkId;

update act_relationships
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name),
    source_archetype_id = substring_index(source_archetype_id, '-', -1),
    target_archetype_id = substring_index(target_archetype_id, '-', -1);

alter table act_relationships
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table action_type_descriptors
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table acts
    add column arch_short_name varchar(100) after linkId,
    add index act_short_name_idx (arch_short_name);

update acts
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table acts
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index act_concept_idx;

alter table archetype_descriptors
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

update assertion_type_descriptors
    set property_archetype = substring_index(property_archetype, '-', -1);

alter table assertion_type_descriptors
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table contacts
    add column arch_short_name varchar(100) after linkId;

update contacts
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table contacts
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table documents
    add column arch_short_name varchar(100) after linkId;

update documents
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table documents
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table entities
    add column arch_short_name varchar(100) after linkId,
    add index entity_arch_sn_name_idx (arch_short_name, name),
    add index entity_short_name_idx (arch_short_name);

update entities
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table entities
    drop index entity_concept_name_idx,
    drop index entity_concept_code_idx,
    drop index entity_concept_idx,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table entity_identities
    add column arch_short_name varchar(100) after linkId,
    add index entity_identity_short_name_idx (arch_short_name);

update entity_identities
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table entity_identities
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table entity_relationships
    add column arch_short_name varchar(100) after linkId;

update entity_relationships
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table entity_relationships
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table granted_authorities
    change arch_short_name archetype varchar(100),
    add column arch_short_name varchar(100) after linkId;

update granted_authorities
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table granted_authorities
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table lookup_relationships
    add column arch_short_name varchar(100) after linkId;

update lookup_relationships
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name),
    source_archetype_id = substring_index(source_archetype_id, '-', -1),
    target_archetype_id = substring_index(target_archetype_id, '-', -1);

alter table lookup_relationships
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table lookups
    add column arch_short_name varchar(100) after linkId;

update lookups
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table lookups
    drop index lookup_concept_value_idx,
    drop index lookup_concept_index,
    drop index lookup_concept_code_idx,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

# remove duplicate PACKET lookup.uom, prior to adding unique index
delete from lookups
where code="PACKET" and arch_short_name="lookup.uom" limit 1;

alter table lookups
    add unique index lookup_short_name_code_idx (arch_short_name,code),
    add index lookup_short_name_index (arch_short_name);

alter table participations
    add column arch_short_name varchar(100) after linkId,
    add index participation_arch_short_name_idx (arch_short_name);

update participations
    set arch_short_name=concat(arch_entity_name, '.', arch_concept),
    entity_archetype_id = substring_index(entity_archetype_id, '-', -1),
    act_archetype_id = substring_index(act_archetype_id, '-', -1);

alter table participations
    drop index participation_arch_concept_idx,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept,
    drop column arch_entity_name,
    drop column sequence,
    drop column mode,
    drop column percentage,
    drop column active_start_time,
    drop column active_end_time;

alter table product_prices
    add column arch_short_name varchar(100) after linkId;

update product_prices
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table product_prices
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

alter table security_roles
    add column arch_short_name varchar(100) after linkId;

update security_roles
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table security_roles
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name;

