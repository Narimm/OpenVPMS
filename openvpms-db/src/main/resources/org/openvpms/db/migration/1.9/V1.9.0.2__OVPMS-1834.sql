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
CREATE TEMPORARY TABLE tmp_reminder_counts (
  relationship_id   BIGINT(20) PRIMARY KEY,
  linkId            VARCHAR(36),
  reminder_type_id  BIGINT(20),
  template_id       BIGINT(20),
  reminder_count_id BIGINT(20),
  reminderCount     INT,
  overdueInterval   INT,
  overdueUnits      VARCHAR(255),
  rule_id           BIGINT(20),
  list              VARCHAR(255),
  export            VARCHAR(255),
  sms               VARCHAR(255),
  INDEX reminder_counts_id_idx(reminder_count_id),
  INDEX reminder_counts_linkId_idx(linkId)
);

INSERT INTO tmp_reminder_counts (relationship_id,
                                 linkId,
                                 reminder_type_id,
                                 template_id,
                                 reminderCount,
                                 overdueInterval,
                                 overdueUnits,
                                 list,
                                 export,
                                 sms)
  SELECT
    r.entity_relationship_id,
    r.linkId,
    r.source_id,
    r.target_id,
    reminderCount.value,
    overdueInterval.value,
    overdueUnits.value,
    list.value,
    export.value,
    sms.value
  FROM entity_relationships r
    JOIN entity_relationship_details reminderCount
      ON r.entity_relationship_id = reminderCount.entity_relationship_id
         AND reminderCount.name = 'reminderCount'
    LEFT JOIN entity_relationship_details overdueInterval
      ON r.entity_relationship_id = overdueInterval.entity_relationship_id
         AND overdueInterval.name = 'interval'
    LEFT JOIN entity_relationship_details overdueUnits
      ON r.entity_relationship_id = overdueUnits.entity_relationship_id
         AND overdueUnits.name = 'units'
    LEFT JOIN entity_relationship_details list
      ON r.entity_relationship_id = list.entity_relationship_id
         AND list.name = 'list'
    LEFT JOIN entity_relationship_details export
      ON r.entity_relationship_id = export.entity_relationship_id
         AND export.name = 'export'
    LEFT JOIN entity_relationship_details sms
      ON r.entity_relationship_id = sms.entity_relationship_id
         AND sms.name = 'sms'
  WHERE r.arch_short_name = 'entityRelationship.reminderTypeTemplate';

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
    'reminderCount',
    'int',
    t.reminderCount
  FROM tmp_reminder_counts t
  WHERE t.reminderCount IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = t.reminder_count_id
                             AND d.name = 'reminderCount');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.reminder_count_id,
    'interval',
    'int',
    t.overdueInterval
  FROM tmp_reminder_counts t
  WHERE t.overdueInterval IS NOT NULL
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = t.reminder_count_id
                             AND d.name = 'interval');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.reminder_count_id,
    'units',
    'string',
    t.overdueUnits
  FROM tmp_reminder_counts t
  WHERE t.overdueUnits IS NOT NULL
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
    t.reminderCount,
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
  WHERE (t.list = 'true' OR t.sms = 'true' OR t.export = 'true') AND NOT exists(
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
    ELSE
      'sms'
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
    'ANY'
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
DELETE r
FROM entity_relationship_details r
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
CREATE TEMPORARY TABLE tmp_reminders (
  act_id      BIGINT(20) PRIMARY KEY,
  createdTime DATETIME,
  nextDueDate DATETIME,
  dueDate     DATETIME
);

INSERT INTO tmp_reminders (act_id, createdTime, nextDueDate, dueDate)
  SELECT
    act_id,
    activity_start_time,
    activity_end_time,
    CASE
    WHEN reminderUnits = 'YEARS'
      THEN date_add(activity_end_time, INTERVAL -reminderInterval * reminderCount YEAR)
    WHEN reminderUnits = 'MONTHS'
      THEN date_add(activity_end_time, INTERVAL -reminderInterval * reminderCount MONTH)
    WHEN reminderUnits = 'DAYS'
      THEN date_add(activity_end_time, INTERVAL -reminderInterval * reminderCount DAY)
    ELSE activity_end_time
    END
  FROM (
         SELECT
           a.act_id,
           a.activity_start_time,
           a.activity_end_time,
           coalesce(reminderCount.value, 0)                      reminderCount,
           coalesce(cast(reminderInterval.value AS UNSIGNED), 0) reminderInterval,
           reminderUnits.value                                   reminderUnits
         FROM acts a
           LEFT JOIN act_details d
             ON a.act_id = d.act_id
                AND d.name = 'createdTime'
           LEFT JOIN act_details reminderCount
             ON a.act_id = reminderCount.act_id
                AND reminderCount.name = 'reminderCount'
           JOIN participations p
             ON a.act_id = p.act_id
                AND p.arch_short_name = 'participation.reminderType'
           JOIN entities reminderType
             ON p.entity_id = reminderType.entity_id
           LEFT JOIN entity_details reminderInterval
             ON reminderInterval.entity_id = reminderType.entity_id
                AND reminderInterval.name = 'interval'
           LEFT JOIN entity_details reminderUnits
             ON reminderUnits.entity_id = reminderType.entity_id
                AND reminderUnits.name = 'units'
         WHERE a.arch_short_name = 'act.patientReminder'
               AND d.name IS NULL) reminder;

INSERT INTO act_details (act_id, name, type, value)
  SELECT
    t.act_id,
    'createdTime',
    'sql-timestamp',
    t.createdTime
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
SET a.activity_start_time = t.nextDueDate,
  a.activity_end_time     = t.dueDate;

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
  ('emailCancelInterval', 'int', '1'),
  ('emailCancelUnits', 'string', 'DAYS'),
  ('emailInterval', 'int', '3'),
  ('emailUnits', 'string', 'DAYS'),
  ('exportCancelInterval', 'int', '5'),
  ('exportCancelUnits', 'string', 'DAYS'),
  ('exportInterval', 'int', '2'),
  ('exportUnits', 'string', 'WEEKS'),
  ('listCancelInterval', 'int', '5'),
  ('listCancelUnits', 'string', 'DAYS'),
  ('listInterval', 'int', '2'),
  ('listUnits', 'string', 'WEEKS'),
  ('printCancelInterval', 'int', '5'),
  ('printCancelUnits', 'string', 'DAYS'),
  ('printInterval', 'int', '2'),
  ('printUnits', 'string', 'WEEKS'),
  ('smsCancelInterval', 'int', '1'),
  ('smsCancelUnits', 'string', 'DAYS'),
  ('smsInterval', 'int', '3'),
  ('smsUnits', 'string', 'DAYS');

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, description, active)
  SELECT
    1,
    UUID(),
    'entity.reminderConfigurationType',
    '1.0',
    'Default Reminder Configuration',
    'Determines the intervals when reminders must be sent and cancelled',
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
