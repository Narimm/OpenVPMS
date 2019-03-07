#
# OVPMS-2134 Add support for mail.smtp.writetimeout
#

INSERT INTO entity_details (entity_id, type, value, name)
  SELECT
    e.entity_id,
    'int',
    '120',
    'timeout'
  FROM entities e
  WHERE e.arch_short_name = 'entity.mailServer'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id
                             AND d.name = 'timeout');
