#
# OVPMS-2045 Add user preference to sort patient history from oldest visit first
#

INSERT INTO entity_details (entity_id, type, value, name)
  SELECT
    e.entity_id,
    'string',
    'DESC',
    'historySort'
  FROM entities e
  WHERE e.arch_short_name = 'entity.preferenceGroupHistory'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id
                             AND d.name = 'historySort');
