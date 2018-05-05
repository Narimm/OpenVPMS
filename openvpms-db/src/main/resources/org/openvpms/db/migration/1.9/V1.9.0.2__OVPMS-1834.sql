# =====================================================================================================================
# Migrate entity.reminderType
# =====================================================================================================================

# rename entity.reminderType group -> groupBy, change true to 'CUSTOMER'
UPDATE entity_details d
  JOIN entities e
    ON d.entity_id = e.entity_id
       AND e.arch_short_name = 'entity.reminderType'
       AND d.name = 'group'
       AND d.value = 'true'
SET d.name = 'groupBy',
  d.type   = 'string',
  d.value  = 'CUSTOMER';

# delete other group node
DELETE d
FROM entity_details d
  JOIN entities e
    ON d.entity_id = e.entity_id
       AND e.arch_short_name = 'entity.reminderType'
       AND d.name = 'group';

# =====================================================================================================================
# Migrate entityRelationship.reminderTypeTemplate
# =====================================================================================================================

DROP TABLE IF EXISTS tmp_reminder_counts;
CREATE TABLE tmp_reminder_counts (
  relationship_id   BIGINT(20) PRIMARY KEY,
  linkId            VARCHAR(36),
  reminder_type_id  BIGINT(20),
  template_id       BIGINT(20),
  reminder_count_id BIGINT(20),
  reminder_count   INT,
  overdue_interval INT,
  overdue_units    VARCHAR(255),
  rule_id           BIGINT(20),
  list              VARCHAR(255),
  export            VARCHAR(255),
  sms               VARCHAR(255),
  INDEX reminder_counts_type_count_idx(reminder_type_id, reminder_count),
  INDEX reminder_counts_id_idx(reminder_count_id),
  INDEX reminder_counts_linkId_idx(linkId)
);

INSERT INTO tmp_reminder_counts (relationship_id,
                                 linkId,
                                 reminder_type_id,
                                 template_id,
                                 reminder_count,
                                 overdue_interval,
                                 overdue_units,
                                 list,
                                 export,
                                 sms)
  SELECT
    r.entity_relationship_id,
    r.linkId,
    r.source_id,
    r.target_id,
    reminder_count.value,
    overdue_interval.value,
    overdue_units.value,
    list.value,
    export.value,
    sms.value
  FROM entity_relationships r
    JOIN entity_relationship_details reminder_count
      ON r.entity_relationship_id = reminder_count.entity_relationship_id
         AND reminder_count.name = 'reminderCount'
    LEFT JOIN entity_relationship_details overdue_interval
      ON r.entity_relationship_id = overdue_interval.entity_relationship_id
         AND overdue_interval.name = 'interval'
    LEFT JOIN entity_relationship_details overdue_units
      ON r.entity_relationship_id = overdue_units.entity_relationship_id
         AND overdue_units.name = 'units'
    LEFT JOIN entity_relationship_details list
      ON r.entity_relationship_id = list.entity_relationship_id
         AND list.name = 'list'
    LEFT JOIN entity_relationship_details export
      ON r.entity_relationship_id = export.entity_relationship_id
         AND export.name = 'export'
    LEFT JOIN entity_relationship_details sms
      ON r.entity_relationship_id = sms.entity_relationship_id
         AND sms.name = 'sms'
  WHERE r.arch_short_name = 'entityRelationship.reminderTypeTemplate'
  GROUP BY r.source_id, reminder_count.value;

#
# Create an entity.reminderCount for each entityRelationship.reminderTypeTemplate
#
INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    0,
    linkId,
    'entity.reminderCount',
    '1.0',
    'Reminder Count',
    1
  FROM tmp_reminder_counts t
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.reminderCount'
            AND e.linkId = t.linkId);

UPDATE tmp_reminder_counts t
  JOIN entities e
    ON t.linkId = e.linkId
       AND e.arch_short_name = 'entity.reminderCount'
SET t.reminder_count_id = e.entity_id;

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.reminder_count_id,
    'count',
    'int',
    t.reminder_count
  FROM tmp_reminder_counts t
  WHERE t.reminder_count IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = t.reminder_count_id
                             AND d.name = 'count');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.reminder_count_id,
    'interval',
    'int',
    t.overdue_interval
  FROM tmp_reminder_counts t
  WHERE t.overdue_interval IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = t.reminder_count_id
                             AND d.name = 'interval');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.reminder_count_id,
    'units',
    'string',
    t.overdue_units
  FROM tmp_reminder_counts t
  WHERE t.overdue_units IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = t.reminder_count_id AND d.name = 'units');

#
# Add entityLink.reminderTypeCount links, linking entity.reminderType to entity.reminderCount
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    t.linkId,
    'entityLink.reminderTypeCount',
    '1.0',
    'Reminder Type Count',
    NULL,
    NULL,
    NULL,
    t.reminder_count,
    t.reminder_type_id,
    t.reminder_count_id
  FROM tmp_reminder_counts t
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.arch_short_name = 'entityLink.reminderTypeCount'
            AND l.source_id = t.reminder_type_id
            AND l.target_id = t.reminder_count_id);

#
# Create an entity.reminderRule for each entityRelationship.reminderTypeTemplate
#
INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    0,
    t.linkId,
    'entity.reminderRule',
    '1.0',
    'Reminder Rule',
    1
  FROM tmp_reminder_counts t
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.reminderRule'
            AND e.linkId = t.linkId);

UPDATE tmp_reminder_counts t
  JOIN entities e
    ON t.linkId = e.linkId
       AND e.arch_short_name = 'entity.reminderRule'
SET t.rule_id = e.entity_id;


INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.rule_id,
    CASE
    WHEN t.list = 'true'
      THEN 'list'
    WHEN t.export = 'true'
      THEN 'export'
    WHEN t.sms = 'true'
      THEN 'sms'
    ELSE
      'contact'
    END,
    'boolean',
    'true'
  FROM tmp_reminder_counts t
  WHERE rule_id IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details
                       WHERE entity_id = t.rule_id);

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.rule_id,
    'sendTo',
    'string',
    'FIRST'
  FROM tmp_reminder_counts t
  WHERE rule_id IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details
                       WHERE entity_id = t.rule_id AND name = 'sendTo');

#
# Link entity.reminderCount to entity.reminderRule
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    t.linkId,
    'entityLink.reminderCountRule',
    '1.0',
    'Reminder Count Rule',
    NULL,
    NULL,
    NULL,
    0,
    t.reminder_count_id,
    t.rule_id
  FROM tmp_reminder_counts t
  WHERE t.rule_id IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.arch_short_name = 'entityLink.reminderCountRule'
                             AND l.source_id = t.reminder_count_id
                             AND l.target_id = t.rule_id);

#
# Link entity.reminderCount to entity.documentTemplate
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    t.linkId,
    'entityLink.reminderCountTemplate',
    '1.0',
    'Reminder Count Template',
    NULL,
    NULL,
    NULL,
    0,
    t.reminder_count_id,
    t.template_id
  FROM tmp_reminder_counts t
  WHERE t.template_id IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.arch_short_name = 'entityLink.reminderCountTemplate'
                             AND l.source_id = t.reminder_count_id
                             AND l.target_id = t.template_id);

#
# Delete entityRelationship.reminderTypeTemplate
#
DELETE d
FROM entity_relationship_details d
  JOIN entity_relationships r
    ON r.entity_relationship_id = d.entity_relationship_id
       AND r.arch_short_name = 'entityRelationship.reminderTypeTemplate'
  JOIN tmp_reminder_counts t
    ON r.entity_relationship_id = t.relationship_id;

DELETE r
FROM entity_relationships r
  JOIN tmp_reminder_counts t
    ON r.entity_relationship_id = t.relationship_id
       AND r.arch_short_name = 'entityRelationship.reminderTypeTemplate';

#
# Migrate reminder dates
#
DROP TABLE IF EXISTS tmp_reminders;
CREATE TABLE tmp_reminders (
  act_id              BIGINT(20) PRIMARY KEY,
  activity_start_time DATETIME,
  activity_end_time   DATETIME,
  reminder_type_id    BIGINT(20),
  reminder_count      INT,
  first_due_date      DATETIME,
  next_due_date       DATETIME
);

INSERT INTO tmp_reminders (act_id, activity_start_time, activity_end_time, first_due_date, reminder_type_id,
                           reminder_count)
  SELECT
    a.act_id,
    a.activity_start_time,
    a.activity_end_time,
    a.activity_end_time,
    p.entity_id,
    coalesce(reminder_count.value, 0) reminder_count
  FROM acts a
    LEFT JOIN act_details d
      ON a.act_id = d.act_id
         AND d.name = 'createdTime'
    LEFT JOIN act_details reminder_count
      ON a.act_id = reminder_count.act_id
         AND reminder_count.name = 'reminderCount'
    JOIN participations p
      ON a.act_id = p.act_id
         AND p.arch_short_name = 'participation.reminderType'
  WHERE a.arch_short_name = 'act.patientReminder'
        AND d.name IS NULL;

#
# Calculate next due date for reminder_count > 0
#
UPDATE tmp_reminders r
  JOIN (
         SELECT
           r.act_id,
           max(t.reminder_count) reminder_count
         FROM tmp_reminders r
           JOIN tmp_reminder_counts t
             ON r.reminder_type_id = t.reminder_type_id
         WHERE t.reminder_count <= r.reminder_count
         GROUP BY r.act_id, r.reminder_type_id) max_count
    ON r.act_id = max_count.act_id
  JOIN tmp_reminder_counts t
    ON r.reminder_type_id = t.reminder_type_id
       AND t.reminder_count = max_count.reminder_count
SET next_due_date = CASE
                    WHEN t.overdue_units = 'YEARS'
                      THEN date_add(r.first_due_date, INTERVAL t.overdue_interval YEAR)
                    WHEN t.overdue_units = 'MONTHS'
                      THEN date_add(r.first_due_date, INTERVAL t.overdue_interval MONTH)
                    WHEN t.overdue_units = 'WEEKS'
                      THEN date_add(r.first_due_date, INTERVAL (t.overdue_interval * 7) DAY)
                    WHEN t.overdue_units = 'DAYS'
                      THEN date_add(r.first_due_date, INTERVAL t.overdue_interval DAY)
                    ELSE r.first_due_date
                    END
WHERE r.reminder_count > 0 AND max_count.reminder_count > 0;

#
# Set next due date for reminder_count=0 or where there is no corresponding tmp_reminder_counts record
#
UPDATE tmp_reminders r
SET next_due_date = first_due_date
WHERE reminder_count = 0
      OR next_due_date IS NULL
      OR next_due_date < first_due_date;

INSERT INTO act_details (act_id, name, type, value)
  SELECT
    t.act_id,
    'createdTime',
    'sql-timestamp',
    t.activity_start_time
  FROM tmp_reminders t
  WHERE NOT exists(
      SELECT *
      FROM act_details d
      WHERE d.act_id = t.act_id
            AND d.name = 'createdTime');

UPDATE acts a
  JOIN tmp_reminders t
    ON a.act_id = t.act_id
       AND a.arch_short_name = 'act.patientReminder'
SET a.activity_start_time = t.next_due_date,
  a.activity_end_time     = t.first_due_date;

DROP TABLE tmp_reminder_counts;
DROP TABLE tmp_reminders;

# =====================================================================================================================
# Create entity.reminderConfigurationType
# =====================================================================================================================

DROP TABLE IF EXISTS tmp_reminder_config;
CREATE TEMPORARY TABLE tmp_reminder_config (
  name  VARCHAR(255) PRIMARY KEY,
  type  VARCHAR(255) NOT NULL,
  value VARCHAR(255) NOT NULL
);

INSERT INTO tmp_reminder_config (name, type, value)
VALUES ('emailAttachments', 'boolean', 'true'),
  ('emailCancelInterval', 'int', '3'),
  ('emailCancelUnits', 'string', 'DAYS'),
  ('emailInterval', 'int', '3'),
  ('emailUnits', 'string', 'DAYS'),
  ('exportCancelInterval', 'int', '2'),
  ('exportCancelUnits', 'string', 'WEEKS'),
  ('exportInterval', 'int', '2'),
  ('exportUnits', 'string', 'WEEKS'),
  ('listCancelInterval', 'int', '2'),
  ('listCancelUnits', 'string', 'WEEKS'),
  ('listInterval', 'int', '2'),
  ('listUnits', 'string', 'WEEKS'),
  ('printCancelInterval', 'int', '2'),
  ('printCancelUnits', 'string', 'WEEKS'),
  ('printInterval', 'int', '2'),
  ('printUnits', 'string', 'WEEKS'),
  ('smsCancelInterval', 'int', '3'),
  ('smsCancelUnits', 'string', 'DAYS'),
  ('smsInterval', 'int', '3'),
  ('smsUnits', 'string', 'DAYS');

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, description, active)
  SELECT
    1,
    UUID(),
    'entity.reminderConfigurationType',
    '1.0',
    'Patient Reminder Configuration',
    'Determines how patient reminders will be processed',
    1
  FROM dual
  WHERE NOT exists(
      SELECT *
      FROM entities
      WHERE arch_short_name = 'entity.reminderConfigurationType');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    t.name,
    t.type,
    t.value
  FROM tmp_reminder_config t
    JOIN entities e
      ON e.arch_short_name = 'entity.reminderConfigurationType'
         AND e.active = 1
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = e.entity_id
                         AND d.name = t.name);
DROP TABLE tmp_reminder_config;

#
# Link entity.reminderConfigurationType to party.organisationPractice
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    1,
    UUID(),
    'entityLink.practiceReminderConfiguration',
    '1.0',
    'Practice Reminder Configuration',
    NULL,
    NULL,
    0,
    practice.entity_id,
    config.entity_id
  FROM entities practice
    JOIN entities config
  WHERE practice.arch_short_name = 'party.organisationPractice'
        AND practice.active = 1
        AND config.arch_short_name = 'entity.reminderConfigurationType'
        AND config.active = 1
        AND NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.arch_short_name = 'entityLink.practiceReminderConfiguration'
            AND l.source_id = practice.entity_id);

#
# Linked entity.reminderConfigurationType to the grouped reminder template
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    UUID(),
    'entityLink.reminderConfigurationTemplateCustomer',
    '1.0',
    'Reminder Configuration Grouped Reminders Template by Customer',
    NULL,
    NULL,
    NULL,
    0,
    config.entity_id,
    template.entity_id
  FROM entities config
    JOIN (
           SELECT max(e.entity_id) entity_id
           FROM entities e
             JOIN entity_details d
               ON d.entity_id = e.entity_id
                  AND d.name = 'archetype'
                  AND d.value = 'GROUPED_REMINDERS'
           WHERE e.arch_short_name = 'entity.documentTemplate'
                 AND e.active = 1) template
  WHERE config.arch_short_name = 'entity.reminderConfigurationType' AND NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.arch_short_name = 'entityLink.reminderConfigurationTemplateCustomer'
            AND l.source_id = config.entity_id);

INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    UUID(),
    'entityLink.reminderConfigurationTemplatePatient',
    '1.0',
    'Reminder Configuration Grouped Reminders Template by Patient',
    NULL,
    NULL,
    NULL,
    0,
    config.entity_id,
    template.entity_id
  FROM entities config
    JOIN (
           SELECT max(e.entity_id) entity_id
           FROM entities e
             JOIN entity_details d
               ON d.entity_id = e.entity_id
                  AND d.name = 'archetype'
                  AND d.value = 'GROUPED_REMINDERS'
           WHERE e.arch_short_name = 'entity.documentTemplate'
                 AND e.active = 1) template
  WHERE config.arch_short_name = 'entity.reminderConfigurationType' AND NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.arch_short_name = 'entityLink.reminderConfigurationTemplatePatient'
            AND l.source_id = config.entity_id);
