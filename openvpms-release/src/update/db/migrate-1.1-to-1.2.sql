# remove the active columns from act_relationships and entity_relationships, for OBF-182

alter table act_relationships
    drop column active;

alter table entity_relationships
    drop column active;


# remove dangling entity relationships for OBF-189/REL-2

delete entity_relationships
from entity_relationships left join entities on source_linkId = entities.linkId
where entities.linkId is null;

delete entity_relationships
from entity_relationships left join entities on target_linkId = entities.linkId
where entities.linkId is null;

# move currency details for OVPMS-759/REL-3

insert into entity_details (entity_id, name, type, value)
select distinct e.entity_id, "currency", "string", d.value
from entities e,
     entities l join entity_details d
         on l.entity_id = d.entity_id
             and l.arch_short_name="party.organisationLocation"
             and d.name="currency"
where e.arch_short_name = "party.organisationPractice";

delete d.*
from entity_details d join entities e
         on e.entity_id = d.entity_id
             and e.arch_short_name = "party.organisationLocation"
             and d.name="currency";