#
#     OVPMS-1976 1.8 to 1.9 migration of act.customerNote -> act.customerCommunicationNote failed to migrate reason
#

UPDATE acts a
  JOIN act_details d
    ON a.act_id = d.act_id
       AND d.name = 'reason'
SET a.reason = d.value
WHERE a.arch_short_name = 'act.customerCommunicationNote'
      AND a.reason IS NULL;

DELETE d
FROM acts a
  JOIN act_details d
    ON a.act_id = d.act_id
WHERE a.arch_short_name = 'act.customerCommunicationNote'
      AND d.name = 'reason';

