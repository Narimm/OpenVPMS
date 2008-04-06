# remove the active columns from act_relationships and entity_relationships, for OBF-182

alter table act_relationships
    drop column active;

alter table entity_relationships
    drop column active;