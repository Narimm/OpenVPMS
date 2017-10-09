#
# OVPMS-1963 Add user preference for boarding period
#
INSERT INTO entity_details (entity_id, type, value, name)
  SELECT
    e.entity_id,
    'string',
    'FORTNIGHT',
    'dates'
  FROM entities e
  WHERE e.arch_short_name = 'entity.preferenceGroupScheduling'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id
                             AND d.name = 'dates');

INSERT INTO entity_details (entity_id, type, value, name)
  SELECT
    e.entity_id,
    'string',
    'ALL',
    'show'
  FROM entities e
  WHERE e.arch_short_name = 'entity.preferenceGroupScheduling'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id
                             AND d.name = 'show');
