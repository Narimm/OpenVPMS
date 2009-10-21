#
# Delete lookup.paymentType as per ARCH-21
#

# remove any entity classifications linked to lookup.paymentType
delete entity_classifications c
from lookups l, entity_classifications c
where c.lookup_id = l.Lookup_id and arch_short_name = "lookup.paymentType";

# remove lookup.paymentType entries
delete
from lookups
where arch_short_name = "lookup.paymentType";

# remove lookup.paymentType archetype
delete d
from assertion_descriptors d, node_descriptors n, archetype_descriptors a
where d.node_desc_id = n.node_desc_id and n.archetype_desc_id = a.archetype_desc_id
      and a.name = "lookup.paymentType.1.0";

delete a, n
from node_descriptors n, archetype_descriptors a
where n.archetype_desc_id = a.archetype_desc_id and a.name = "lookup.paymentType.1.0";


#
# Migrate party.customerorganisation to party.customerperson as per ARCH-16
#
insert into entity_details (entity_id, type, value, name)
select entity_id, "string", name, "companyName"
from entities
where arch_short_name = "party.customerorganisation";

insert into entity_details (entity_id, type, value, name)
select entity_id, "string", name, "lastName"
from entities
where arch_short_name = "party.customerorganisation";

update entities set arch_short_name = "party.customerperson"
where arch_short_name = "party.customerorganisation";

# remove party.customerorganisation archetype
delete d
from assertion_descriptors d, node_descriptors n, archetype_descriptors a
where d.node_desc_id = n.node_desc_id and n.archetype_desc_id = a.archetype_desc_id
      and a.name = "party.customerorganisation.1.0";

delete a, n
from node_descriptors n, archetype_descriptors a
where n.archetype_desc_id = a.archetype_desc_id and a.name = "party.customerorganisation.1.0";

#
# Migrate act.patientInvestigationBiochemistry, act.patientInvestigationCytology,
# act.patientInvestigationHaematology and act.patientInvestigationRadiology
# to act.patientInvestigation, as per REL-7
#
drop table if exists investigations;
create table investigations (
    act_id bigint not null);

# find all act.patientInvestigationBiochemistry that dont have a participation.investigationType
insert into investigations (act_id)
select a.act_id
from acts a
left join participations p on a.act_id = p.act_id and p.arch_short_name = "participation.investigationType"
where a.arch_short_name = "act.patientInvestigationBiochemistry"
      and p.arch_short_name is null;

# create a biochemistry investigation type if needed
insert into entities (version, linkId, arch_short_name, arch_version, name, active)
select 1, UUID(), "entity.investigationType", "1.0", "Biochemistry - Migrated", 1
from dual
where not exists (
   select *
   from entities
   where arch_short_name = "entity.investigationType" and name = "Biochemistry - Migrated")
and exists (select * from investigations);

# add a participation between each act.patientInvestigationBiochemistry act and the biochemistry entity.investigationType
insert into participations (version, linkId, arch_short_name, arch_version, active, act_arch_short_name, entity_id, act_id)
select 1, UUID(), "participation.investigationType", "1.0", 1, "act.patientInvestigation", e.entity_id, i.act_id
from investigations i, entities e
where e.arch_short_name = "entity.investigationType" and e.name = "Biochemistry - Migrated";

delete from investigations;

# find all act.patientInvestigationCytology that dont have a participation.investigationType
insert into investigations (act_id)
select a.act_id
from acts a
left join participations p on a.act_id = p.act_id and p.arch_short_name = "participation.investigationType"
where a.arch_short_name = "act.patientInvestigationCytology"
      and p.arch_short_name is null;

# create a cytology investigation type, if needed
insert into entities (version, linkId, arch_short_name, arch_version, name, active)
select 1, UUID(), "entity.investigationType", "1.0", "Cytology - Migrated", 1
from dual
where not exists (
   select *
   from entities
   where arch_short_name = "entity.investigationType" and name = "Cytology - Migrated")
and exists (select * from investigations);

# add a participation between each act.patientInvestigationCytology act and the cytology entity.investigationType
insert into participations (version, linkId, arch_short_name, arch_version, active, act_arch_short_name, entity_id, act_id)
select 1, UUID(), "participation.investigationType", "1.0", 1, "act.patientInvestigation", e.entity_id, i.act_id
from investigations i, entities e
where e.arch_short_name = "entity.investigationType" and e.name = "Cytology - Migrated";

delete from investigations;

# find all act.patientInvestigationHaematology that dont have a participation.investigationType
insert into investigations (act_id)
select a.act_id
from acts a
left join participations p on a.act_id = p.act_id and p.arch_short_name = "participation.investigationType"
where a.arch_short_name = "act.patientInvestigationHaematology"
      and p.arch_short_name is null;

# create a haematology investigation type if required
insert into entities (version, linkId, arch_short_name, arch_version, name, active)
select 1, UUID(), "entity.investigationType", "1.0", "Haemotology - Migrated", 1
from dual
where not exists (
   select *
   from entities
   where arch_short_name = "entity.investigationType" and name = "Haemotology - Migrated")
and exists (select * from investigations);

# add a participation between each act.patientInvestigationHaematology act and the haematology entity.investigationType
insert into participations (version, linkId, arch_short_name, arch_version, active, act_arch_short_name, entity_id, act_id)
select 1, UUID(), "participation.investigationType", "1.0", 1, "act.patientInvestigation", e.entity_id, i.act_id
from investigations i, entities e
where e.arch_short_name = "entity.investigationType" and e.name = "Haemotology - Migrated";

delete from investigations;

# find all act.patientInvestigationRadiology that dont have a participation.investigationType
insert into investigations (act_id)
select a.act_id
from acts a
left join participations p on a.act_id = p.act_id and p.arch_short_name = "participation.investigationType"
where a.arch_short_name = "act.patientInvestigationRadiology"
      and p.arch_short_name is null;

# create a radiology investigation type if required
insert into entities (version, linkId, arch_short_name, arch_version, name, active)
select 1, UUID(), "entity.investigationType", "1.0", "Radiology - Migrated", 1
from dual
where not exists (
   select *
   from entities
   where arch_short_name = "entity.investigationType" and name = "Radiology - Migrated")
and exists (select * from investigations);

# add a participation between each act.patientInvestigationBiochemistry act and the radiology entity.investigationType
insert into participations (version, linkId, arch_short_name, arch_version, active, act_arch_short_name, entity_id, act_id)
select 1, UUID(), "participation.investigationType", "1.0", 1, "act.patientInvestigation", e.entity_id, i.act_id
from investigations i, entities e
where e.arch_short_name = "entity.investigationType" and e.name = "Radiology - Migrated";

drop table investigations;

update acts set arch_short_name = "act.patientInvestigation"
where arch_short_name in ("act.patientInvestigationBiochemistry", 
                          "act.patientInvestigationCytology", 
                          "act.patientInvestigationHaematology",
                          "act.patientInvestigationRadiology");

update participations set act_arch_short_name = "act.patientInvestigation"
where act_arch_short_name in ("act.patientInvestigationBiochemistry", 
                              "act.patientInvestigationCytology", 
                              "act.patientInvestigationHaematology",
                              "act.patientInvestigationRadiology");
