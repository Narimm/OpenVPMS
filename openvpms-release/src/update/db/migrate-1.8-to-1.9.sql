#
# Security authorities for OVPMS-1646 Practice Location branding for 1.9 archetypes
#
DROP TABLE IF EXISTS new_authorities;
CREATE TEMPORARY TABLE new_authorities (
  name        VARCHAR(255) PRIMARY KEY,
  description VARCHAR(255),
  method      VARCHAR(255),
  archetype   VARCHAR(255)
);

INSERT INTO new_authorities (name, description, method, archetype)
VALUES ('Document Logo Act Create', 'Authority to Create Document Logo Act', 'create', 'act.documentLogo'),
  ('Document Logo Act Save', 'Authority to Save Document Logo Act', 'save', 'act.documentLogo'),
  ('Document Logo Act Remove', 'Authority to Remove Document Logo Act', 'remove', 'act.documentLogo');


INSERT INTO granted_authorities (version, linkId, arch_short_name, arch_version, name, description, active, service_name, method, archetype)
  SELECT
    0,
    UUID(),
    'security.archetypeAuthority',
    '1.0',
    a.name,
    a.description,
    1,
    'archetypeService',
    a.method,
    a.archetype
  FROM new_authorities a
  WHERE NOT exists(
      SELECT *
      FROM granted_authorities g
      WHERE g.name = a.name);

INSERT INTO roles_authorities (security_role_id, authority_id)
  SELECT
    r.security_role_id,
    g.granted_authority_id
  FROM security_roles r
    JOIN granted_authorities g
    JOIN new_authorities a
      ON a.name = g.name
  WHERE r.name = 'Base Role' AND NOT exists
  (SELECT *
   FROM roles_authorities x
   WHERE x.security_role_id = r.security_role_id AND x.authority_id = g.granted_authority_id);

DROP TABLE new_authorities;

#
# Ensure idealQty >= criticalQty for OVPMS-1678 Add validation to idealQty and criticalQty nodes of
# entityRelationship.productStockLocation
#
DROP TABLE IF EXISTS ideal_critical_qty;
CREATE TEMPORARY TABLE ideal_critical_qty (
  id          BIGINT NOT NULL PRIMARY KEY,
  idealQty    DECIMAL(18, 3),
  criticalQty DECIMAL(18, 3)
);

INSERT INTO ideal_critical_qty (id, idealQty, criticalQty)
  SELECT
    r.entity_relationship_id,
    cast(idealQty.value AS DECIMAL(18, 3)),
    cast(criticalQty.value AS DECIMAL(18, 3))
  FROM entity_relationships r
    JOIN entity_relationship_details criticalQty
      ON r.entity_relationship_id = criticalQty.entity_relationship_id
         AND criticalQty.name = 'criticalQty'
    JOIN entity_relationship_details idealQty
      ON r.entity_relationship_id = idealQty.entity_relationship_id
         AND idealQty.name = 'idealQty'
         AND idealQty.value < criticalQty.value
  WHERE r.arch_short_name = 'entityRelationship.productStockLocation';

UPDATE entity_relationships r
  JOIN ideal_critical_qty i
    ON i.id = r.entity_relationship_id
  JOIN entity_relationship_details criticalQty
    ON r.entity_relationship_id = criticalQty.entity_relationship_id
       AND criticalQty.name = 'criticalQty'
  JOIN entity_relationship_details idealQty
    ON r.entity_relationship_id = idealQty.entity_relationship_id
       AND idealQty.name = 'idealQty'
SET idealQty.value  = i.criticalQty,
  criticalQty.value = i.idealQty;

DROP TABLE ideal_critical_qty;

#
# Insert template include print flag for OVPMS-1664 Product templates created prior to 1.8 display Print flag as unset
#
INSERT INTO entity_link_details (id, type, value, name)
  SELECT
    l.id,
    'boolean',
    'true',
    'print'
  FROM entity_links l
  WHERE l.arch_short_name = 'entityLink.productIncludes'
        AND NOT exists(SELECT *
                       FROM entity_link_details d
                       WHERE d.id = l.id
                             AND d.name = 'print');


#
# Migrate mail servers for OVPMS-1097 SMS Appointment reminders (Automated)
#
DROP TABLE IF EXISTS tmp_mail_servers;

CREATE TEMPORARY TABLE tmp_mail_servers (
  location_id   BIGINT NOT NULL PRIMARY KEY,
  location_name VARCHAR(255),
  entity_id     BIGINT NULL,
  linkId        VARCHAR(36),
  host          VARCHAR(255),
  port          INT,
  username      VARCHAR(255),
  password      VARCHAR(255),
  security      VARCHAR(255),
  INDEX tmp_mail_server_entity_id_idx(entity_id)
);

INSERT INTO tmp_mail_servers (location_id, location_name, linkId, host, port, username, password, security)
  SELECT
    e.entity_id,
    e.name,
    UUID(),
    mailHost.value,
    mailPort.value,
    mailUsername.value,
    mailPassword.value,
    mailSecurity.value
  FROM entities e
    JOIN entity_details mailHost
      ON e.entity_id = mailHost.entity_id AND mailHost.name = 'mailHost'
    LEFT JOIN entity_details mailPort
      ON e.entity_id = mailPort.entity_id AND mailPort.name = 'mailPort'
    LEFT JOIN entity_details mailUsername
      ON e.entity_id = mailUsername.entity_id AND mailUsername.name = 'mailUsername'
    LEFT JOIN entity_details mailPassword
      ON e.entity_id = mailPassword.entity_id AND mailPassword.name = 'mailPassword'
    LEFT JOIN entity_details mailSecurity
      ON e.entity_id = mailSecurity.entity_id AND mailSecurity.name = 'mailSecurity' AND mailSecurity.value <> 'NONE'
  WHERE e.arch_short_name = 'party.organisationLocation' AND e.active = 1;

#
# Create entity.mailServer instances for each mail server configured on a practice location
#
INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.mailServer',
    '1.0',
    concat(location_name, ' - ', host),
    1
  FROM tmp_mail_servers t
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.mailServer'
            AND e.linkId = t.linkId);

UPDATE tmp_mail_servers t
  JOIN entities e
    ON t.linkId = e.linkId AND e.arch_short_name = 'entity.mailServer'
SET t.entity_id = e.entity_id;

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'host',
    'string',
    t.host
  FROM tmp_mail_servers t
  WHERE t.host IS NOT NULL AND NOT exists(SELECT *
                                          FROM entity_details d
                                          WHERE d.entity_id = t.entity_id AND d.name = 'host');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'port',
    'int',
    t.port
  FROM tmp_mail_servers t
  WHERE t.port IS NOT NULL AND NOT exists(SELECT *
                                          FROM entity_details d
                                          WHERE d.entity_id = t.entity_id AND d.name = 'port');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'username',
    'string',
    t.username
  FROM tmp_mail_servers t
  WHERE t.username IS NOT NULL AND NOT exists(SELECT *
                                              FROM entity_details d
                                              WHERE d.entity_id = t.entity_id AND d.name = 'username');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'password',
    'string',
    t.password
  FROM tmp_mail_servers t
  WHERE t.password IS NOT NULL AND NOT exists(SELECT *
                                              FROM entity_details d
                                              WHERE d.entity_id = t.entity_id AND d.name = 'password');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'security',
    'string',
    t.security
  FROM tmp_mail_servers t
  WHERE t.security IS NOT NULL AND NOT exists(SELECT *
                                              FROM entity_details d
                                              WHERE d.entity_id = t.entity_id AND d.name = 'security');

#
# Link the locations to their mail servers
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    linkId,
    'entityLink.organisationMailServer',
    '1.0',
    'Organisation Mail Server',
    NULL,
    NULL,
    NULL,
    0,
    t.location_id,
    t.entity_id
  FROM tmp_mail_servers t
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = t.location_id);

#
# Link the practice to the first mail server
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    t.linkId,
    'entityLink.organisationMailServer',
    '1.0',
    'Organisation Mail Server',
    NULL,
    NULL,
    NULL,
    0,
    p.entity_id,
    t.entity_id
  FROM tmp_mail_servers t
    JOIN entities p ON p.arch_short_name = 'party.organisationPractice' AND p.active = 1
    JOIN entities e ON e.entity_id = (SELECT min(entity_id)
                                      FROM entities
                                      WHERE arch_short_name = 'entity.mailServer' AND active = 1)
                       AND e.entity_id = t.entity_id
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = p.entity_id AND l.arch_short_name = 'entityLink.organisationMailServer');


DELETE d
FROM entity_details d
  JOIN tmp_mail_servers t
    ON d.entity_id = t.location_id
       AND d.name IN ('mailHost', 'mailPort', 'mailUsername', 'mailPassword', 'mailSecurity');

DROP TABLE tmp_mail_servers;

#
# Link schedules to locations, where only one relationship exists.
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    UUID(),
    'entityLink.scheduleLocation',
    '1.0',
    'Schedule Location',
    NULL,
    NULL,
    NULL,
    0,
    schedule.entity_id,
    location.entity_id
  FROM entities location
    JOIN entity_relationships lr
      ON location.entity_id = lr.source_id
         AND location.arch_short_name = 'party.organisationLocation'
         AND lr.arch_short_name = 'entityRelationship.locationView'
    JOIN entities scheduleView
      ON lr.target_id = scheduleView.entity_id
    JOIN entity_relationships ls
      ON scheduleView.entity_id = ls.source_id
         AND ls.arch_short_name = 'entityRelationship.viewSchedule'
    JOIN entities schedule
      ON ls.target_id = schedule.entity_id
  WHERE location.active = 1
        AND scheduleView.active = 1
        AND schedule.active = 1
        AND NOT exists
  (
      SELECT *
      FROM entity_links l
      WHERE l.source_id = schedule.entity_id
            AND l.target_id = location.entity_id
            AND l.arch_short_name = 'entityLink.scheduleLocation')
  GROUP BY schedule.entity_id
  HAVING count(location.entity_id) = 1;

#
# Set up a default entity.documentTemplateSMSAppointment, if one isn't present
#
INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, description, active)
  SELECT
    1,
    UUID(),
    'entity.documentTemplateSMSAppointment',
    '1.0',
    'Default Appointment Reminder SMS Template',
    '<patient>''s appointment at <location> is confirmed for <date/time>. Call us on <phone> if you need to change the appointment',
    1
  FROM dual
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.documentTemplateSMSAppointment');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'expressionType',
    'string',
    'XPATH'
  FROM entities e
  WHERE e.arch_short_name = 'entity.documentTemplateSMSAppointment' AND NOT exists(SELECT *
                                                                                   FROM entity_details d
                                                                                   WHERE d.entity_id = e.entity_id AND
                                                                                         d.name = 'expressionType');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'expression',
    'string',
    'concat(expr:if(expr:var(''patient.name'') != '''', concat(expr:var(''patient.name''), ''&quot;s''), ''Your''),
                     '' appointment at '' , $location.name,'' is confirmed for '', date:formatDate($appointment.startTime, ''short''),
                     '' @ '', date:formatTime($appointment.startTime, ''short''), $nl,
                     ''Call us on '', party:getTelephone($location), '' if you need to change the appointment'')'
  FROM entities e
  WHERE e.arch_short_name = 'entity.documentTemplateSMSAppointment' AND NOT exists(SELECT *
                                                                                   FROM entity_details d
                                                                                   WHERE d.entity_id = e.entity_id AND
                                                                                         d.name = 'expression');
