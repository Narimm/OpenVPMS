# increase the property_map column size, for OBF-172

alter table assertion_descriptors
    modify property_map mediumtext;

# set the active flag for inactive entity relationships, for OVPMS-728

update entity_relationships
    set active=0 where active_end_time is not null