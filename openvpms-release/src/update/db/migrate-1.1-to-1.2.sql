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
