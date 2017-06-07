#
# Migrate lookup.patientAlertType to entity.patientAlertType.
# All but the ALLERGY and AGGRESSION lookup.patientAlertTypes are retained; these are used for HL7 and SmartFlow
#


DROP TABLE IF EXISTS tmp_alerts;
CREATE TEMPORARY TABLE tmp_alerts (
  lookup_id BIGINT(20) PRIMARY KEY,
  entity_id BIGINT(20),
  INDEX tmp_alerts_idx (entity_id)
);

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    l.linkId,
    'entity.patientAlertType',
    '1.0',
    l.name,
    l.active
  FROM lookups l
  WHERE l.arch_short_name = 'lookup.patientAlertType'
        AND NOT exists(SELECT *
                       FROM entities e
                       WHERE e.linkId = l.linkId AND e.arch_short_name = 'entity.patientAlertType');

INSERT INTO entity_classifications (entity_id, lookup_id)
  SELECT
    a.entity_id,
    a.lookup_id
  FROM tmp_alerts a
    JOIN lookups l
      ON l.lookup_id = a.lookup_id
         AND l.code IN ('AGGRESSION', 'ALLERGY')
         AND NOT exists(SELECT *
                        FROM entity_classifications ec
                        WHERE ec.entity_id = a.entity_id AND ec.lookup_id = a.lookup_id);

INSERT INTO tmp_alerts (lookup_id, entity_id)
  SELECT
    l.lookup_id,
    e.entity_id
  FROM lookups l
    JOIN entities e
      ON l.linkId = e.linkId
         AND l.arch_short_name = 'lookup.patientAlertType'
         AND e.arch_short_name = 'entity.patientAlertType';


INSERT INTO entity_details (entity_id, type, value, name)
  SELECT
    alert.entity_id,
    ld.type,
    ld.value,
    ld.name
  FROM entities alert
    JOIN tmp_alerts ta
      ON alert.entity_id = ta.entity_id
    JOIN lookups l
      ON ta.lookup_id = l.lookup_id
    JOIN lookup_details ld
      ON l.lookup_id = ld.lookup_id
         AND NOT exists(SELECT *
                        FROM entities e
                          JOIN entity_details ed
                            ON e.entity_id = ed.entity_id
                        WHERE e.entity_id = alert.entity_id AND ed.name = ld.name);

INSERT INTO participations (version, linkId, arch_short_name, arch_version, active, act_arch_short_name, entity_id,
                            act_id, activity_start_time, activity_end_time)
  SELECT
    1,
    UUID(),
    'participation.patientAlertType',
    '1.0',
    1,
    'act.patientAlert',
    ta.entity_id,
    a.act_id,
    a.activity_start_time,
    a.activity_end_time
  FROM acts a
    JOIN act_details d
      ON a.act_id = d.act_id
         AND d.name = 'alertType'
    JOIN lookups l
      ON l.arch_short_name = 'lookup.patientAlertType'
         AND l.code = d.value
    JOIN tmp_alerts ta
      ON l.lookup_id = ta.lookup_id
  WHERE a.arch_short_name = 'act.patientAlert'
        AND NOT exists(SELECT *
                       FROM participations p
                       WHERE p.act_id = a.act_id AND p.arch_short_name = 'participation.patientAlertType');

DELETE d
FROM lookup_details d
  JOIN lookups l
    ON d.lookup_id = l.lookup_id
       AND l.arch_short_name = 'lookup.patientAlertType'
       AND l.code NOT IN ('AGGRESSION', 'ALLERGY');


DELETE l
FROM lookups l
WHERE l.arch_short_name = 'lookup.patientAlertType'
      AND l.code NOT IN ('AGGRESSION', 'ALLERGY');

DROP TABLE tmp_alerts;