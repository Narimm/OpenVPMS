# clean out archetypes

delete from assertion_descriptors;
delete from node_descriptors;
delete from archetype_descriptors;

# act_relationships

alter table act_relationships
    add column source_id bigint(20) after parent_child_relationship,
    add column target_id bigint(20) after source_id,
    drop column source_arch_short_name,
    drop column target_arch_short_name,
    drop index linkId,
    add index FK70AA344EFCECFA9B (target_id),
    add index FK70AA344EC84DFFD1 (source_id),
    add constraint FK70AA344EC84DFFD1 foreign key (source_id)
        references acts (act_id) on delete cascade,
    add constraint FK70AA344EFCECFA9B foreign key (target_id)
        references acts (act_id) on delete cascade;

update act_relationships r, acts a
    set r.source_id = a.act_id
where r.source_linkId = a.linkId;

update act_relationships r, acts a
    set r.target_id = a.act_id
where r.target_linkId = a.linkId;

alter table act_relationships
    drop column source_linkId,
    drop column target_linkId;

delete r, d
from act_relationships r, act_relationship_details d
where r.act_relationship_id = d.act_relationship_id
    and (r.source_id is null or r.target_id is null);

# action_type_descriptors

alter table action_type_descriptors
    drop index linkId;

# archetype_descriptors

alter table archetype_descriptors
    modify column class_name varchar(255) not null,
    drop index linkId;

# assertion_descriptors

alter table assertion_descriptors
    drop index linkId;

# assertion_type_descriptors

alter table assertion_type_descriptors
    drop index linkId;

# audit_records

alter table audit_records
    add column linkId varchar(36) not null after version;

# contacts

alter table contacts
    drop index linkId;

# document_acts

alter table document_acts
    add column document_id bigint(20) after docref_arch_short_name;

update document_acts a, documents d
    set a.document_id = d.document_id
where a.docref_linkId = d.linkId;

alter table document_acts
    drop column docref_arch_short_name,
    drop column docref_linkId,
    add key FK5E78FAC592203729 (document_id),
    add constraint FK5E78FAC592203729 foreign key (document_id)
        references documents (document_id);

# documents

alter table documents
    drop index linkId;

# entity_identities

alter table entity_identities
    drop index linkId;

# entity_relationships

alter table entity_relationships
    add column source_id bigint(20) after active_end_time,
    add column target_id bigint(20) after source_id,
    drop column source_arch_short_name,
    drop column target_arch_short_name;

update entity_relationships r, entities e
    set r.source_id = e.entity_id
where r.source_linkId = e.linkId;

update entity_relationships r, entities e
    set r.target_id = e.entity_id
where r.target_linkId = e.linkId;

alter table entity_relationships
    drop column source_linkId,
    drop column target_linkId,
    drop index linkId,
    add index FK861BFDDFA5B2869 (source_id),
    add index FK861BFDDF3EFA2333 (target_id),
    add constraint FK861BFDDFA5B2869 foreign key (source_id)
        references entities (entity_id) on delete cascade,
    add constraint FK861BFDDF3EFA2333 foreign key (target_id)
        references entities (entity_id) on delete cascade;

delete r, d
from entity_relationships r, entity_relationship_details d
where r.entity_relationship_id = d.entity_relationship_id
    and (source_id is null or target_id is null);

# granted_authorities

alter table granted_authorities
    drop column prefix,
    drop index linkId,
    drop column authority;

# lookup_relationships

alter table lookup_relationships
    add column source_id bigint(20) after target_linkId,
    add column target_id bigint(20) after source_id;

update lookup_relationships r, lookups l
    set r.source_id = l.lookup_id
where r.source_linkId = l.linkId;

update lookup_relationships r, lookups l
    set r.target_id = l.lookup_id
where r.target_linkId = l.linkId;

alter table lookup_relationships
    drop column source_linkId,
    drop column target_linkId,
    drop column source_arch_short_name,
    drop column target_arch_short_name,
    drop index linkId,
    add key FK2C88AF36F5B96353 (target_id),
    add key FK2C88AF36C11A6889 (source_id),
    add constraint FK2C88AF36C11A6889 foreign key (source_id)
        references lookups (lookup_id) on delete cascade,
    add constraint FK2C88AF36F5B96353 foreign key (target_id)
        references lookups (lookup_id) on delete cascade;


# lookups

alter table lookups
    drop index linkId;

# node_descriptors

alter table node_descriptors
    drop index linkId;

# participations

alter table participations
    add column entity_id bigint(20) after active,
    add column act_id bigint(20) after entity_id,
    add column activity_start_time datetime,
    add column activity_end_time datetime,
    drop column entity_arch_short_name,
    drop index linkId;

update participations p, entities e
    set p.entity_id = e.entity_id
where p.entity_linkId = e.linkId;

update participations p, acts a
    set p.act_id = a.act_id, p.activity_start_time = a.activity_start_time,
        p.activity_end_time = a.activity_end_time
where p.act_linkId = a.linkId;

alter table participations
    drop column entity_linkId,
    drop column act_linkId,
    add key FKA301B52D8B907FA (act_id),
    add key FKA301B524372B7A1 (entity_id),
    add constraint FKA301B524372B7A1
        foreign key (entity_id) references entities (entity_id),
    add constraint FKA301B52D8B907FA
        foreign key (act_id) references acts (act_id) on delete cascade,
    add index participation_entity_start_time_idx (entity_id, activity_start_time),
    add index participation_entity_end_time_idx (entity_id, activity_end_time),
    add index participation_act_entity_start_time_idx (act_arch_short_name, entity_id, activity_start_time);

delete from participations
    where entity_id is null or act_id is null;

# product_prices

alter table product_prices
    drop index linkId;

# security_roles

alter table security_roles
    drop index linkId;

# acts

alter table acts
    drop index linkId;

# entities

alter table entities
    drop index linkId;

# etl_log

drop table if exists etl_log;

