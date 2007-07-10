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
    modify linkId varchar(36) not null,
    modify arch_version varchar(100) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify source_linkId varchar(36),
    change source_archetype_id source_arch_short_name varchar(100),
    modify target_linkId varchar(36),
    change target_archetype_id target_arch_short_name varchar(100);

update act_relationships
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name),
    source_arch_short_name = substring_index(substring_index(source_arch_short_name, '-', -1), '.', 2),
    target_arch_short_name = substring_index(substring_index(target_arch_short_name, '-', -1), '.', 2);

alter table act_relationships
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop column sequence,
    drop index act_relationship_id_idx,
    add unique key (linkId);

alter table action_type_descriptors
    modify linkId varchar(36) not null,
    modify arch_short_name varchar(100) not null,
    modify arch_version varchar(100) not null,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
	drop index action_type_desc_id_idx,
    add unique key (linkId);

alter table acts
    modify linkId varchar(36) not null,
    modify arch_version varchar(100) not null,
    add column arch_short_name varchar(100) not null after linkId;

update acts
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table acts
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index act_id_idx,
    drop index act_concept_idx,
    drop index act_linkId_idx,
    add unique key (linkId),
    add index act_short_name_idx (arch_short_name);

update archetype_descriptors
    set name = substring_index(name, '-', -1);

alter table archetype_descriptors
    modify linkId varchar(36) not null,
    modify name varchar(100) not null,
    modify arch_short_name varchar(100) not null,
    modify arch_version varchar(100) not null,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index archetype_desc_id_idx,
    add unique key linkId (linkId);

alter table assertion_descriptors
    modify linkId varchar(36) not null,
    drop index assertion_desc_id_idx,
    add unique key (linkId);

update assertion_type_descriptors
    set property_archetype = substring_index(property_archetype, '-', -1);

alter table assertion_type_descriptors
    modify linkId varchar(36) not null,
    modify arch_short_name varchar(100) not null,
    modify arch_version varchar(100) not null,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index assertion_type_id_idx,
    add unique key (linkId);

alter table audit_records
    drop index audit_id_idx;

alter table contacts
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update contacts
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table contacts
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index contact_id_idx,
    add unique key (linkId);

alter table document_acts
    change docref_archetype_id docref_arch_short_name varchar(100),
    modify docref_linkId varchar(36);

update document_acts
    set docref_arch_short_name=substring_index(substring_index(docref_arch_short_name, '-', -1), '.', 2);

alter table documents
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update documents
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table documents
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index document_id_idx,
    add unique key (linkId);

alter table entities
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update entities
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table entities
    drop index entity_concept_name_idx,
    drop index entity_concept_code_idx,
    drop index entity_concept_idx,
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index entity_id_idx,
    drop index entity_linkId_idx,
    add unique key (linkId),
    add index entity_arch_sn_name_idx (arch_short_name, name),
    add index entity_short_name_idx (arch_short_name);

alter table entity_identities
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update entity_identities
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table entity_identities
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index entity_identity_id_idx,
    add unique key (linkId);

alter table entity_relationships
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null,
    modify column source_linkId varchar(36),
    change column source_archetype_id source_arch_short_name varchar(100),
    modify column target_linkId varchar(36),
    change column target_archetype_id target_arch_short_name varchar(100);

update entity_relationships
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name),
    source_arch_short_name = substring_index(substring_index(source_arch_short_name, '-', -1), '.', 2),
    target_arch_short_name = substring_index(substring_index(target_arch_short_name, '-', -1), '.', 2);

alter table entity_relationships
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop column sequence,
    drop column reason,
    drop index entity_relationship_id_idx,
    add unique key (linkId);

alter table granted_authorities
    modify linkId varchar(36) not null,
    change arch_short_name archetype varchar(100),
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update granted_authorities
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table granted_authorities
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index granted_authority_id_idx,
    add unique key (linkId);

alter table lookup_relationships
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null,
    modify column source_linkId varchar(36),
    change column source_archetype_id source_arch_short_name varchar(100),
    modify column target_linkId varchar(36),
    change column target_archetype_id target_arch_short_name varchar(100);

update lookup_relationships
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name),
    source_arch_short_name = substring_index(substring_index(source_arch_short_name, '-', -1), '.', 2),
    target_arch_short_name = substring_index(substring_index(target_arch_short_name, '-', -1), '.', 2);

alter table lookup_relationships
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index lookup_relationship_id_idx,
    add unique key (linkId);

alter table lookups
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

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

#
# To find other duplicate lookups:
# select arch_short_name, code
# from lookups
# group by arch_short_name, code
# having count(code) > 1
#

alter table lookups
    drop index lookup_id_idx,
    add unique key (linkId),
    add unique index lookup_short_name_code_idx (arch_short_name,code),
    add index lookup_short_name_index (arch_short_name);

alter table node_descriptors
    modify linkId varchar(36) not null,
    change default_name default_value varchar(255),
    drop index node_desc_id_idx,
    add unique key (linkId);

alter table participations
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null,
    modify column entity_linkId varchar(36),
    change entity_archetype_id entity_arch_short_name varchar(100),
    modify column act_linkId varchar(36),
    change act_archetype_id act_arch_short_name varchar(100);

update participations
    set arch_short_name=concat(arch_entity_name, '.', arch_concept),
    entity_arch_short_name = substring_index(substring_index(entity_arch_short_name, '-', -1), '.', 2),
    act_arch_short_name = substring_index(substring_index(act_arch_short_name, '-', -1), '.', 2);

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
    drop column active_end_time,
    drop index participation_id_idx,
    drop index participation_linkId_idx,
    drop index participation_entity_linkId_idx,
    add unique key (linkId),
    add index participation_arch_short_name_idx (arch_short_name),
    add index participation_entity_linkId_act_sn_index (entity_linkId, act_arch_short_name);

alter table product_prices
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update product_prices
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table product_prices
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index product_price_id_idx,
    add unique key (linkId);

alter table security_roles
    modify linkId varchar(36) not null,
    add column arch_short_name varchar(100) not null after linkId,
    modify arch_version varchar(100) not null;

update security_roles
    set arch_short_name=concat(arch_entity_name, '.', arch_concept_name);

alter table security_roles
    drop column arch_namespace,
    drop column arch_rm_name,
    drop column arch_concept_name,
    drop column arch_entity_name,
    drop index security_role_id_idx,
    add unique key (linkId);

