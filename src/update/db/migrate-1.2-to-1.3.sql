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

delete d
from act_relationships r, act_relationship_details d
where r.act_relationship_id = d.act_relationship_id
    and (r.source_id is null or r.target_id is null);

delete r
from act_relationships r
where r.source_id is null or r.target_id is null;

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
    add column sequence integer not null after active_end_time,
    add column source_id bigint(20) after sequence,
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

delete d
from entity_relationships r, entity_relationship_details d
where r.entity_relationship_id = d.entity_relationship_id
    and (source_id is null or target_id is null);

delete r
from entity_relationships r
where source_id is null or target_id is null;

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

#
# migrate postcodes, for OVPMS-501
#

# Create a temporary table for suburb information

drop table if exists suburb;
create table suburb (
    code varchar(255) not null,
    name varchar(255) not null,
    postcode varchar(255) not null,
    state varchar(255) not null);

# populate suburb table based on current suburb, postcode and state information
# in contacts

insert into suburb (code, name, postcode, state)
select distinct concat(upper(suburb.value), "_", postcode.value) as code,
       suburb.value suburb, postcode.value postcode, state.value state
from contacts c
join contact_details suburb on c.contact_id = suburb.contact_id and suburb.name="suburb"
join contact_details postcode on c.contact_id = postcode.contact_id and postcode.name="postcode"
join contact_details state on c.contact_id = state.contact_id and state.name="state";

# generate valid codes

update suburb
  set code = replace(code, " ", "_");

update suburb
  set code = replace(code, ".", "_");

update suburb
  set code = replace(code, "&", "_");

update suburb
  set code = replace(code, "/", "_");

update suburb
  set code = replace(code, "\\", "_");

update suburb
  set code = replace(code, "(", "_");

update suburb
  set code = replace(code, ")", "_");

update suburb
  set code = replace(code, "*", "_");

update suburb
  set code = replace(code, ",", "_");

update suburb
  set code = replace(code, "-", "_");

update suburb
  set code = replace(code, "[", "_");

update suburb
  set code = replace(code, "]", "_");

update suburb
  set code = replace(code, "__", "_");

# update contacts suburb value to code
update contact_details c1, contact_details c2, suburb s
set c1.value = s.code
where c1.value  = s.name and c2.value = s.postcode
and c1.name="suburb" and c2.name="postcode"
and c1.contact_id = c2.contact_id;

# create suburb lookup
insert into lookups (version, linkId, arch_short_name, active, arch_version,
    code, name, description, default_lookup)
select 0, UUID(), "lookup.suburb", 1, "1.0", code, name, null, 0
from suburb
group by code;

insert into lookup_details (lookup_id, name, value, type)
select lookup_id, "postCode", s.postcode, "string"
from lookups l, suburb s
where l.code = s.code
group by s.code;

# link suburbs to states
insert into lookup_relationships (version, linkId, arch_short_name,
    arch_version, active, source_id, target_id)
select 0 as version, UUID() as linkId, "lookupRelationship.stateSuburb" as arch_short_name,
       "1.0" as arch_version, 1 as active, source.lookup_id as source_id,
       target.lookup_id as target_id
from suburb
join lookups source on suburb.state = source.code
join lookups target on suburb.code = target.code
where source.arch_short_name = "lookup.state"
    and target.arch_short_name = "lookup.suburb"
group by suburb.code;

drop table suburb;

#
# Remove redundant location schedule relationships for OVPMS-792
#

delete d
from entity_relationships r, entity_relationship_details d
where r.entity_relationship_id = d.entity_relationship_id
    and r.arch_short_name = "entityRelationship.locationSchedule";

delete r
from entity_relationships r
where r.arch_short_name = "entityRelationship.locationSchedule";

#
# Remove redundant location work list relationships for OVPMS-793
#

delete d
from entity_relationships r, entity_relationship_details d
where r.entity_relationship_id = d.entity_relationship_id
    and r.arch_short_name = "entityRelationship.locationWorkList";

delete r
from entity_relationships r
where r.arch_short_name = "entityRelationship.locationWorkList";

#
# Add fixed_cost and unit_cost to financial_acts for OBF-192
#
alter table financial_acts
    add column fixed_cost decimal(18, 3) after unit_amount,
    add column unit_cost decimal(18, 3) after fixed_cost;
