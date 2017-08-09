#
# OVPMS-1922 Change reminder item date behaviour
#

DROP TABLE IF EXISTS tmp_reminder_type_count;
DROP TABLE IF EXISTS tmp_reminder_item;

#
# Stores reminder type/reminder count information.
#
CREATE TEMPORARY TABLE tmp_reminder_type_count (
  entity_id        BIGINT(20),
  reminder_count   INT,
  overdue_interval INT,
  overdue_units    VARCHAR(255),
  PRIMARY KEY (entity_id, reminder_count)
);

#
# Stores information for act.patientReminderItem* acts that need to be migrated.
#
CREATE TEMPORARY TABLE tmp_reminder_item (
  act_id           BIGINT(20) PRIMARY KEY,
  reminder_type_id BIGINT(20),
  reminder_count   INT,
  send_date        DATETIME,
  due_date         DATETIME,
  processed_date   DATETIME,
  lead_interval    INT,
  lead_units       VARCHAR(255)
);

DROP PROCEDURE IF EXISTS sp_reminder_lead_time;
DROP PROCEDURE IF EXISTS sp_reminder_calc_send_date;

#
# Determines the lead time for a given reminder type.
#
# . reminder_type - one of 'email', 'sms', 'print', 'export', 'list'
# . lead_interval - returns the lead interval
# . lead_units    - returns the lead interval units

DELIMITER $$
CREATE PROCEDURE sp_reminder_lead_time(
  IN    reminder_type VARCHAR(255),
  INOUT lead_interval INT,
  INOUT lead_units    VARCHAR(255))
  BEGIN
    SELECT
      reminder_lead_interval.value,
      reminder_lead_units.value
    INTO lead_interval, lead_units
    FROM entities practice
      JOIN entity_links lreminder
        ON lreminder.source_id = practice.entity_id
           AND lreminder.arch_short_name = 'entityLink.practiceReminderConfiguration'
      JOIN entities config
        ON lreminder.target_id = config.entity_id
           AND config.arch_short_name = 'entity.reminderConfigurationType'
           AND config.active = 1
      JOIN entity_details reminder_lead_interval
        ON reminder_lead_interval.entity_id = config.entity_id
           AND reminder_lead_interval.name = concat(reminder_type, 'Interval')
      JOIN entity_details reminder_lead_units
        ON reminder_lead_units.entity_id = config.entity_id
           AND reminder_lead_units.name = concat(reminder_type, 'Units')
    WHERE practice.arch_short_name = 'party.organisationPractice'
          AND practice.active = 1;
  END $$

#
# Calculates the tmp_reminder_item send_date, for all items with the specified archetype and reminder type.
#
# . archetype the reminder item archetype
# . reminder_type - one of 'email', 'sms', 'print', 'export', 'list'
#
CREATE PROCEDURE sp_reminder_calc_send_date(IN archetype VARCHAR(255), IN reminder_type VARCHAR(255))
  BEGIN
    DECLARE lead_interval INT;
    DECLARE lead_units VARCHAR(255);
    CALL sp_reminder_lead_time(reminder_type, lead_interval, lead_units);
    IF (lead_interval IS NOT NULL && lead_units IS NOT NULL)
    THEN
      UPDATE tmp_reminder_item item
        JOIN acts a
          ON item.act_id = a.act_id
             AND a.arch_short_name = archetype
             AND a.status <> 'PENDING'
      SET item.send_date   = CASE
                             WHEN lead_units = 'MONTHS'
                               THEN date_add(item.due_date, INTERVAL -lead_interval MONTH)
                             WHEN lead_units = 'WEEKS'
                               THEN date_add(item.due_date, INTERVAL (-lead_interval * 7) DAY)
                             WHEN lead_units = 'DAYS'
                               THEN date_add(item.due_date, INTERVAL -lead_interval DAY)
                             ELSE item.send_date
                             END,
        item.lead_interval = lead_interval,
        item.lead_units    = lead_units;
    END IF;
  END $$

DELIMITER ;

#
# Populate the tmp_reminder_type_count table with all entity.reminderType instances that have associated
# entity.reminderCount instances.
#
INSERT INTO tmp_reminder_type_count (entity_id, reminder_count, overdue_interval, overdue_units)
  SELECT
    reminder_type.entity_id,
    reminder_count_count.value,
    reminder_count_interval.value,
    reminder_count_units.value units
  FROM entities reminder_type
    JOIN entity_links lreminder_count
      ON lreminder_count.source_id = reminder_type.entity_id
    JOIN entities reminder_count
      ON lreminder_count.target_id = reminder_count.entity_id
         AND reminder_count.arch_short_name = 'entity.reminderCount'
    JOIN entity_details reminder_count_count
      ON reminder_count_count.entity_id = reminder_count.entity_id
         AND reminder_count_count.name = 'count'
    JOIN entity_details reminder_count_interval
      ON reminder_count_interval.entity_id = reminder_count.entity_id
         AND reminder_count_interval.name = 'interval'
    JOIN entity_details reminder_count_units
      ON reminder_count_units.entity_id = reminder_count.entity_id
         AND reminder_count_units.name = 'units'
  WHERE reminder_type.arch_short_name = 'entity.reminderType';

#
# Populate the tmp_reminder_item table.
#
INSERT INTO tmp_reminder_item (act_id, reminder_type_id, reminder_count, send_date, due_date)
  SELECT
    item.act_id,
    preminder_type.entity_id,
    reminder_count.value reminder_count,
    item.activity_start_time,
    CASE
    WHEN reminder_type_count.entity_id IS NULL OR reminder_count.value = 0
      THEN reminder.activity_end_time
    ELSE
      CASE
      WHEN reminder_type_count.overdue_units = 'YEARS'
        THEN date_add(reminder.activity_end_time, INTERVAL reminder_type_count.overdue_interval YEAR)
      WHEN reminder_type_count.overdue_units = 'MONTHS'
        THEN date_add(reminder.activity_end_time, INTERVAL reminder_type_count.overdue_interval MONTH)
      WHEN reminder_type_count.overdue_units = 'WEEKS'
        THEN date_add(reminder.activity_end_time, INTERVAL (reminder_type_count.overdue_interval * 7) DAY)
      WHEN reminder_type_count.overdue_units = 'DAYS'
        THEN date_add(reminder.activity_end_time, INTERVAL reminder_type_count.overdue_interval DAY)
      ELSE reminder.activity_end_time
      END
    END                  due_date
  FROM acts reminder
    JOIN act_relationships r
      ON r.source_id = reminder.act_id
         AND r.arch_short_name = 'actRelationship.patientReminderItem'
    JOIN acts item
      ON r.target_id = item.act_id
    JOIN act_details reminder_count
      ON item.act_id = reminder_count.act_id
         AND reminder_count.name = 'count'
    JOIN participations preminder_type
      ON preminder_type.act_id = reminder.act_id
         AND preminder_type.arch_short_name = 'participation.reminderType'
    LEFT JOIN tmp_reminder_type_count reminder_type_count
      ON reminder_type_count.entity_id = preminder_type.entity_id
         AND reminder_count.value = reminder_type_count.reminder_count
  WHERE reminder.arch_short_name = 'act.patientReminder';

#
# Set the processed_date for each reminder item.
#
UPDATE tmp_reminder_item item
  JOIN acts act
    ON item.act_id = act.act_id
SET item.processed_date = act.activity_start_time
WHERE act.status <> 'PENDING';

#
# Calculate the send_date for each reminder item, based on their due dates and the lead time for each reminder type.
#
CALL sp_reminder_calc_send_date('act.patientReminderItemEmail', 'email');
CALL sp_reminder_calc_send_date('act.patientReminderItemSMS', 'sms');
CALL sp_reminder_calc_send_date('act.patientReminderItemPrint', 'print');
CALL sp_reminder_calc_send_date('act.patientReminderItemExport', 'export');
CALL sp_reminder_calc_send_date('act.patientReminderItemList', 'list');

#
# Migrate the actual act.patientReminderItem*
#
UPDATE acts a
  JOIN tmp_reminder_item item
    ON a.act_id = item.act_id
       AND a.arch_short_name IN
           ('act.patientReminderItemEmail', 'act.patientReminderItemSMS', 'act.patientReminderItemPrint',
            'act.patientReminderItemExport', 'act.patientReminderItemList')
SET a.activity_start_time = item.send_date,
  a.activity_end_time     = item.due_date;

INSERT INTO act_details (act_id, name, type, value)
  SELECT
    item.act_id,
    'processed',
    'sql-timestamp',
    item.processed_date
  FROM tmp_reminder_item item
  WHERE item.processed_date IS NOT NULL && NOT exists(
      SELECT *
      FROM act_details d
      WHERE d.act_id = item.act_id
            AND d.name = 'processed');

DROP PROCEDURE sp_reminder_lead_time;
DROP PROCEDURE sp_reminder_calc_send_date;
DROP TABLE tmp_reminder_type_count;
DROP TABLE tmp_reminder_item;
