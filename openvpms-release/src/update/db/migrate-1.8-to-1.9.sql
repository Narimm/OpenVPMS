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
# entityLink.productStockLocation
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
  WHERE r.arch_short_name = 'entityLink.productStockLocation'
        AND cast(idealQty.value AS DECIMAL(18, 3)) < cast(criticalQty.value AS DECIMAL(18, 3));

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
      WHERE l.source_id = t.location_id AND l.arch_short_name = 'entityLink.organisationMailServer');

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
            AND l.arch_short_name = 'entityLink.scheduleLocation')
  GROUP BY schedule.entity_id
  HAVING count(*) = 1;

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
    'concat(expr:if(expr:var(''patient.name'') != '''', concat(expr:var(''patient.name''), "&apos;s"), ''Your''),
                     '' appointment at '' , $location.name,'' is confirmed for '', date:formatDate($appointment.startTime, ''short''),
                     '' @ '', date:formatTime($appointment.startTime, ''short''), $nl,
                     ''Call us on '', party:getTelephone($location), '' if you need to change the appointment'')'
  FROM entities e
  WHERE e.arch_short_name = 'entity.documentTemplateSMSAppointment' AND NOT exists(SELECT *
                                                                                   FROM entity_details d
                                                                                   WHERE d.entity_id = e.entity_id AND
                                                                                         d.name = 'expression');

#
# Update entity.HL7Mapping* to include a sendADT node for OVPMS-1704 Add support to enable/disable ADT messages for
# IDEXX.
# This only applies to sites that have installed a pre-release version of OpenVPMS 1.9
#
INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'sendADT',
    'boolean',
    'true'
  FROM entities e
  WHERE e.arch_short_name IN ('entity.HL7Mapping', 'entity.HL7MappingCubex', 'entity.HL7MappingIDEXX') AND
        NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = e.entity_id AND
                         d.name = 'sendADT');
#
# Update entity.productDose to include a quantity node for OVPMS-1677 Add dose number to product dosing
#
# This only applies to sites that have installed a pre-release version of OpenVPMS 1.9
#
INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'quantity',
    'big-decimal',
    '1.00'
  FROM entities e
  WHERE e.arch_short_name = 'entity.productDose' AND
        NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = e.entity_id AND d.name = 'quantity');

#
# Rename roundType -> roundTo as per the entity.productDose archetype
#
UPDATE entity_details d
  JOIN entities e
    ON d.entity_id = e.entity_id
       AND e.arch_short_name = 'entity.productDose'
       AND d.name = 'roundType'
SET d.name = 'roundTo';


#
# Replace entityRelationship.productStockLocation with entityLink.productStockLocation
# for OVPMS-1570 Replace entity relationships between products and stock locations with an entity link
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    version,
    linkId,
    'entityLink.productStockLocation',
    '1.0',
    name,
    description,
    active_start_time,
    active_end_time,
    sequence,
    source_id,
    target_id
  FROM entity_relationships r
  WHERE r.arch_short_name = 'entityRelationship.productStockLocation'
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.source_id = r.source_id
                             AND l.target_id = r.target_id
                             AND (l.active_start_time = r.active_start_time OR
                                  (l.active_start_time IS NULL AND l.active_start_time IS NULL))
                             AND (l.active_end_time = r.active_end_time OR
                                  (l.active_end_time IS NULL AND l.active_end_time IS NULL))
                             AND l.arch_short_name = 'entityLink.productStockLocation');

INSERT INTO entity_link_details (id, name, type, value)
  SELECT
    l.id,
    d.name,
    d.type,
    d.value
  FROM entity_relationships r
    JOIN entity_relationship_details d
      ON r.entity_relationship_id = d.entity_relationship_id
    JOIN entity_links l
      ON l.arch_short_name = 'entityLink.productStockLocation'
         AND l.source_id = r.source_id AND l.target_id = r.target_id
         AND (l.active_start_time = r.active_start_time
              OR (l.active_start_time IS NULL AND l.active_start_time IS NULL))
         AND (l.active_end_time = r.active_end_time
              OR (l.active_end_time IS NULL AND l.active_end_time IS NULL))
  WHERE r.arch_short_name = 'entityRelationship.productStockLocation'
        AND NOT exists(SELECT *
                       FROM entity_link_details ld
                       WHERE ld.id = l.id AND ld.name = d.name);

# Remove the old relationships
DELETE d
FROM entity_relationship_details d
  JOIN entity_relationships r
    ON d.entity_relationship_id = r.entity_relationship_id
WHERE r.arch_short_name = 'entityRelationship.productStockLocation';

DELETE r
FROM entity_relationships r
WHERE r.arch_short_name = 'entityRelationship.productStockLocation';

#
# Replace entityRelationship.productTypeProduct with entityLink.productType for
# OVPMS-1567 Replace entity relationship between products and product types with an entity link
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    version,
    linkId,
    'entityLink.productType',
    '1.0',
    name,
    description,
    active_start_time,
    active_end_time,
    sequence,
    target_id,
    source_id
  FROM entity_relationships r
  WHERE r.arch_short_name = 'entityRelationship.productTypeProduct'
        AND NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = r.target_id
            AND l.target_id = r.source_id
            AND
            (l.active_start_time = r.active_start_time OR (l.active_start_time IS NULL AND l.active_start_time IS NULL))
            AND (l.active_end_time = r.active_end_time OR (l.active_end_time IS NULL AND l.active_end_time IS NULL))
            AND l.arch_short_name = 'entityLink.productType');

INSERT INTO entity_link_details (id, name, type, value)
  SELECT
    l.id,
    d.name,
    d.type,
    d.value
  FROM entity_relationships r
    JOIN entity_relationship_details d
      ON r.entity_relationship_id = d.entity_relationship_id
    JOIN entity_links l
      ON l.arch_short_name = 'entityLink.productType'
         AND l.source_id = r.target_id AND l.target_id = r.source_id
         AND
         (l.active_start_time = r.active_start_time OR (l.active_start_time IS NULL AND l.active_start_time IS NULL))
         AND (l.active_end_time = r.active_end_time OR (l.active_end_time IS NULL AND l.active_end_time IS NULL))
  WHERE r.arch_short_name = 'entityRelationship.productTypeProduct'
        AND NOT exists(
      SELECT *
      FROM entity_link_details ld
      WHERE ld.id = l.id AND ld.name = d.name);

# Remove the old relationships
DELETE d
FROM entity_relationship_details d
  JOIN entity_relationships r
    ON d.entity_relationship_id = r.entity_relationship_id
WHERE r.arch_short_name = 'entityRelationship.productTypeProduct';

DELETE r
FROM entity_relationships r
WHERE r.arch_short_name = 'entityRelationship.productTypeProduct';

#
# OBF-239 Increase length of node_descriptors defaultValue and derivedValue fields
#
DELIMITER $$
CREATE PROCEDURE OBF239_modify_node_descriptors()
  BEGIN
    DECLARE _count INT;
    SET _count = (SELECT count(*)
                  FROM INFORMATION_SCHEMA.COLUMNS
                  WHERE
                    TABLE_NAME = 'node_descriptors' AND TABLE_SCHEMA = DATABASE() AND COLUMN_NAME = 'default_value' AND
                    CHARACTER_MAXIMUM_LENGTH < 5000);
    IF _count = 1
    THEN
      ALTER TABLE node_descriptors MODIFY default_value VARCHAR(5000);
      ALTER TABLE node_descriptors MODIFY derived_value VARCHAR(5000);
    END IF;
  END $$
DELIMITER ;

CALL OBF239_modify_node_descriptors();
DROP PROCEDURE OBF239_modify_node_descriptors;

#
# OVPMS-1569 Replace entity relationships between products and suppliers with an entity link
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    version,
    linkId,
    'entityLink.productSupplier',
    '1.0',
    name,
    description,
    active_start_time,
    active_end_time,
    sequence,
    source_id,
    target_id
  FROM entity_relationships r
  WHERE r.arch_short_name = 'entityRelationship.productSupplier'
        AND NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = r.source_id
            AND l.target_id = r.target_id
            AND l.linkId = r.linkId
            AND l.arch_short_name = 'entityLink.productSupplier');

INSERT INTO entity_link_details (id, name, type, value)
  SELECT
    l.id,
    d.name,
    d.type,
    d.value
  FROM entity_relationships r
    JOIN entity_relationship_details d
      ON r.entity_relationship_id = d.entity_relationship_id
    JOIN entity_links l
      ON l.arch_short_name = 'entityLink.productSupplier'
         AND l.source_id = r.source_id
         AND l.target_id = r.target_id
         AND l.linkId = r.linkId
  WHERE r.arch_short_name = 'entityRelationship.productSupplier'
        AND NOT exists(
      SELECT *
      FROM entity_link_details ld
      WHERE ld.id = l.id AND ld.name = d.name);

# Remove the old relationships
DELETE d
FROM entity_relationship_details d
  JOIN entity_relationships r
    ON d.entity_relationship_id = r.entity_relationship_id
WHERE r.arch_short_name = 'entityRelationship.productSupplier';

DELETE r
FROM entity_relationships r
WHERE r.arch_short_name = 'entityRelationship.productSupplier';

#
# OVPMS-1722 Customer communications log
#
INSERT INTO document_acts (document_act_id, doc_version, file_name, mime_type, printed, document_id)
  SELECT
    a.act_id,
    0,
    NULL,
    'text/plain',
    FALSE,
    NULL
  FROM acts a
  WHERE a.arch_short_name = 'act.customerNote'
        AND NOT exists(SELECT *
                       FROM document_acts d
                       WHERE document_act_id = a.act_id);

UPDATE participations p
SET p.act_arch_short_name = 'act.customerCommunicationNote'
WHERE p.act_arch_short_name = 'act.customerNote';

UPDATE act_details d
  JOIN acts a
    ON a.act_id = d.act_id
SET d.name = 'message'
WHERE a.arch_short_name = 'act.customerNote' AND d.name = 'note';

UPDATE act_details d
  JOIN acts a
    ON a.act_id = d.act_id
SET d.name = 'reason'
WHERE a.arch_short_name = 'act.customerNote' AND d.name = 'category';

UPDATE acts a
SET a.arch_short_name = 'act.customerCommunicationNote'
WHERE a.arch_short_name = 'act.customerNote';

UPDATE acts a
  JOIN act_details d
    ON a.act_id = d.act_id
       AND d.name = 'message'
       AND a.arch_short_name = 'act.customerCommunicationNote'
SET a.description = CASE
                    WHEN locate('\n', d.value) > 255
                      THEN concat(substring(d.value, 1, 252), '...')
                    WHEN locate('\n', d.value) > 0
                      THEN substring(d.value, 1, locate('\n', d.value))
                    WHEN char_length(d.value) > 255
                      THEN concat(substring(d.value, 1, 252), '...')
                    ELSE
                      d.value
                    END
WHERE a.description IS NULL;

UPDATE lookups l
SET l.arch_short_name = 'lookup.customerCommunicationReason'
WHERE l.arch_short_name = 'lookup.customerNoteCategory';

INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.customerCommunicationReason',
    1,
    '1.0',
    'AD_HOC_EMAIL',
    'Ad hoc Email',
    NULL,
    0
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.customerCommunicationReason' AND e.code = 'AD_HOC_EMAIL');

INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.customerCommunicationReason',
    1,
    '1.0',
    'AD_HOC_SMS',
    'Ad hoc SMS',
    NULL,
    0
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.customerCommunicationReason' AND e.code = 'AD_HOC_SMS');

INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.customerCommunicationReason',
    1,
    '1.0',
    'PATIENT_REMINDER',
    'Patient reminder',
    NULL,
    0
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.customerCommunicationReason' AND e.code = 'PATIENT_REMINDER');

INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.customerCommunicationReason',
    1,
    '1.0',
    'FORWARDED_DOCUMENT',
    'Forwarded Document',
    NULL,
    0
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.customerCommunicationReason'
                         AND e.code = 'FORWARDED_DOCUMENT');

# Migrate granted_authorities
UPDATE granted_authorities g
SET archetype = 'act.customerCommunication*'
WHERE archetype = 'act.customerNote';

#
# Migrate entity.documentTemplate emailSubject and emailText for to entity.documentTemplateEmail for
# OVPMS-1729 HTML emails
#
DROP TABLE IF EXISTS tmp_email_templates;
CREATE TEMPORARY TABLE tmp_email_templates (
  linkId    VARCHAR(36) PRIMARY KEY,
  source_id BIGINT(20) NOT NULL,
  entity_id BIGINT(20),
  name      VARCHAR(255),
  subject   VARCHAR(5000),
  text      VARCHAR(5000),
  active    BIT(1)
);

INSERT INTO tmp_email_templates (linkId, source_id, name, subject, text, active)
  SELECT
    e.linkId,
    e.entity_id,
    e.name,
    if(isnull(emailSubject.value), e.name, emailSubject.value),
    emailText.value,
    e.active
  FROM entities e
    LEFT JOIN entity_details emailSubject
      ON e.entity_id = emailSubject.entity_id
         AND emailSubject.name = 'emailSubject'
    JOIN entity_details emailText
      ON e.entity_id = emailText.entity_id
         AND emailText.name = 'emailText'
  WHERE e.arch_short_name = 'entity.documentTemplate';

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.documentTemplateEmail',
    '1.0',
    name,
    active
  FROM tmp_email_templates t
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.documentTemplateEmail'
            AND e.linkId = t.linkId);

UPDATE tmp_email_templates t
  JOIN entities e
    ON t.linkId = e.linkId
       AND e.arch_short_name = 'entity.documentTemplateEmail'
SET t.entity_id = e.entity_id;

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'subject',
    'string',
    t.subject
  FROM tmp_email_templates t
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = t.entity_id AND d.name = 'subject');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'contentType',
    'string',
    'TEXT'
  FROM tmp_email_templates t
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = t.entity_id AND d.name = 'contentType');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'content',
    'string',
    t.text
  FROM tmp_email_templates t
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = t.entity_id AND d.name = 'content');

#
# Link the templates to their email templates
#
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    linkId,
    'entityLink.documentTemplateEmail',
    '1.0',
    'Email Template',
    NULL,
    NULL,
    NULL,
    0,
    t.source_id,
    t.entity_id
  FROM tmp_email_templates t
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = t.source_id AND l.arch_short_name = 'entityLink.documentTemplateEmail');

DELETE d
FROM entity_details d
  JOIN tmp_email_templates t
    ON d.entity_id = t.source_id
       AND d.name IN ('emailSubject', 'emailText');

DROP TABLE tmp_email_templates;


#
# OVPMS-1744 Medical Record Locking
#

#
# Set the status node for existing medical records if they have none.
#
UPDATE acts a
  JOIN act_relationships r
    ON r.arch_short_name = 'actRelationship.patientClinicalEventItem'
       AND a.act_id = r.target_id
       AND a.status IS NULL
SET a.status = 'IN_PROGRESS';

# Add a status2 column to acts.
DELIMITER $$
CREATE PROCEDURE OVPMS_1744_modify_acts()
  BEGIN
    DECLARE _count INT;
    SET _count = (SELECT count(*)
                  FROM INFORMATION_SCHEMA.COLUMNS
                  WHERE
                    TABLE_NAME = 'acts' AND TABLE_SCHEMA = DATABASE() AND COLUMN_NAME = 'status2');
    IF _count = 0
    THEN
      ALTER TABLE acts
      ADD COLUMN status2 VARCHAR(50)
      AFTER status,
      ADD INDEX act_short_name_status2_idx (arch_short_name, status2);
    END IF;
  END $$
DELIMITER ;

CALL OVPMS_1744_modify_acts();
DROP PROCEDURE OVPMS_1744_modify_acts;

# Migrate act.patientInvestigation acts to set the status and status2 nodes
UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status2 = 'PENDING'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'IN_PROGRESS'
      AND a.status2 IS NULL
      AND d.document_id IS NULL;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status2 = 'RECEIVED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'IN_PROGRESS'
      AND a.status2 IS NULL
      AND d.document_id IS NOT NULL;

UPDATE acts a
SET a.status = 'IN_PROGRESS',
  a.status2  = 'RECEIVED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'RECEIVED'
      AND a.status2 IS NULL;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status = 'POSTED',
  a.status2  = 'REVIEWED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'COMPLETED'
      AND a.status2 IS NULL
      AND d.printed = TRUE;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status = 'IN_PROGRESS',
  a.status2  = 'RECEIVED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'COMPLETED'
      AND a.status2 IS NULL
      AND d.printed = FALSE;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status2 = 'REVIEWED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'CANCELLED'
      AND a.status2 IS NULL
      AND d.printed = TRUE;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status2 = 'PENDING'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'CANCELLED'
      AND a.status2 IS NULL
      AND d.printed = FALSE;

UPDATE acts a
SET a.status = 'IN_PROGRESS',
  a.status2  = 'RECEIVED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'PRELIMINARY'
      AND a.status2 IS NULL;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status = 'POSTED',
  a.status2  = 'REVIEWED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'FINAL'
      AND d.printed = TRUE
      AND a.status2 IS NULL;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status = 'POSTED',
  a.status2  = 'RECEIVED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'FINAL'
      AND a.status2 IS NULL
      AND d.printed = FALSE;

#
# OVPMS-1720 Smart Flow Sheet Improvement Stage 1
#

# Migrate createFlowSheet
UPDATE entity_details d
  JOIN entities e
    ON d.entity_id = e.entity_id
       AND d.name = 'createFlowSheet'
       AND d.type = 'boolean'
       AND d.value = 'true'
       AND e.arch_short_name = 'party.organisationWorkList'
SET d.type = 'string',
  d.value  = 'DEFAULT';

DELETE d
FROM entity_details d
  JOIN entities e
    ON d.entity_id = e.entity_id
       AND d.name = 'createFlowSheet'
       AND d.type = 'boolean'
       AND d.value = 'false'
       AND e.arch_short_name = 'party.organisationWorkList';


#
# OVPMS-1767 Support macro expansion in SMS reminders
#

# Migrate entity.documentTemplate sms node to entity.documentTemplateSMSReminder
DROP TABLE IF EXISTS tmp_sms_templates;
CREATE TEMPORARY TABLE tmp_sms_templates (
  linkId    VARCHAR(36) PRIMARY KEY,
  source_id BIGINT(20) NOT NULL,
  entity_id BIGINT(20),
  name      VARCHAR(255),
  text      VARCHAR(5000),
  active    BIT(1)
);

INSERT INTO tmp_sms_templates (linkId, source_id, name, text, active)
  SELECT
    e.linkId,
    e.entity_id,
    e.name,
    sms.value,
    e.active
  FROM entities e
    JOIN entity_details sms
      ON e.entity_id = sms.entity_id
         AND sms.name = 'sms'
  WHERE e.arch_short_name = 'entity.documentTemplate';

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.documentTemplateSMSReminder',
    '1.0',
    name,
    active
  FROM tmp_sms_templates t
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.documentTemplateSMSReminder'
            AND e.linkId = t.linkId);

UPDATE tmp_sms_templates t
  JOIN entities e
    ON t.linkId = e.linkId
       AND e.arch_short_name = 'entity.documentTemplateSMSReminder'
SET t.entity_id = e.entity_id;

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'content',
    'string',
    t.text
  FROM tmp_sms_templates t
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = t.entity_id AND d.name = 'content');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    t.entity_id,
    'contentType',
    'string',
    'TEXT'
  FROM tmp_sms_templates t
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = t.entity_id AND d.name = 'contentType');

# Link the templates to their SMS templates
INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    linkId,
    'entityLink.documentTemplateSMS',
    '1.0',
    'SMS Template',
    NULL,
    NULL,
    NULL,
    0,
    t.source_id,
    t.entity_id
  FROM tmp_sms_templates t
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = t.source_id AND l.arch_short_name = 'entityLink.documentTemplateSMS');

DELETE d
FROM entity_details d
  JOIN tmp_sms_templates t
    ON d.entity_id = t.source_id
       AND d.name IN ('sms');

DROP TABLE tmp_sms_templates;

# Set up a default entity.documentTemplateSMSReminder, if one isn't present
INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, description, active)
  SELECT
    1,
    UUID(),
    'entity.documentTemplateSMSReminder',
    '1.0',
    'Sample Vaccination Reminder SMS Template',
    '<patient> is due for a vaccination. Call us on <phone> to make an appointment',
    1
  FROM dual
  WHERE NOT exists(
      SELECT *
      FROM entities e
      WHERE e.arch_short_name = 'entity.documentTemplateSMSReminder');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'contentType',
    'string',
    'XPATH'
  FROM entities e
  WHERE e.arch_short_name = 'entity.documentTemplateSMSReminder' AND NOT exists(SELECT *
                                                                                FROM entity_details d
                                                                                WHERE d.entity_id = e.entity_id AND
                                                                                      d.name = 'contentType');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'content',
    'string',
    'concat($patient.name, '' is due for a vaccination.'', $nl, ''Please contact us on '',  party:getTelephone($location), '' to make an appointment'')'
  FROM entities e
  WHERE e.arch_short_name = 'entity.documentTemplateSMSReminder' AND NOT exists(SELECT *
                                                                                FROM entity_details d
                                                                                WHERE d.entity_id = e.entity_id AND
                                                                                      d.name = 'content');

# Make entity.documentTemplateSMSAppointment have the same nodes as entity.documentTemplateSMSReminder
UPDATE entity_details expressionType
  JOIN entities e ON expressionType.entity_id = e.entity_id AND expressionType.name = "expressionType"
                     AND e.arch_short_name = 'entity.documentTemplateSMSAppointment'
SET expressionType.name = 'contentType';

UPDATE entity_details expression
  JOIN entities e ON expression.entity_id = e.entity_id AND expression.name = "expression"
                     AND e.arch_short_name = 'entity.documentTemplateSMSAppointment'
SET expression.name = 'content';


#
# OVPMS-1763 Schedule blocking
#

# Rename act.customerAppointmentSeries to act.calendarEventSeries
UPDATE acts a
SET a.arch_short_name = 'act.calendarEventSeries'
WHERE a.arch_short_name = 'act.customerAppointmentSeries';

UPDATE participations p
SET p.act_arch_short_name = 'act.scheduleEventSeries'
WHERE p.act_arch_short_name = 'act.customerAppointmentSeries';

# Rename actRelationship.customerAppointmentSeries to actRelationship.calendarEventSeries
UPDATE act_relationships r
SET r.arch_short_name = 'actRelationship.calendarEventSeries'
WHERE r.arch_short_name = 'actRelationship.customerAppointmentSeries';

#
# OVPMS-1769 Distinguish between SMS appointment reminders and general SMSes in communications log
#
INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.customerCommunicationReason',
    1,
    '1.0',
    'APPOINTMENT_REMINDER',
    'Appointment Reminder',
    NULL,
    0
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.customerCommunicationReason' AND e.code = 'APPOINTMENT_REMINDER');
