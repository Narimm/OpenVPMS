#
# Insert parts = 1 for every entity.SMSConfigEmail* that doesn't have one.
#

INSERT INTO entity_details (entity_id, type, value, name)
  SELECT
    e.entity_id,
    'int',
    '1',
    'parts'
  FROM entities e
  WHERE e.arch_short_name LIKE 'entity.SMSConfigEmail%'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id
                             AND d.name = 'parts');
