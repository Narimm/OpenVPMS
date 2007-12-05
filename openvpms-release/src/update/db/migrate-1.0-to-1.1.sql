# increase the property_map column size, for OBF-172

alter table assertion_descriptors
    modify property_map mediumtext;