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
  WHERE e.arch_short_name = 'entity.documentTemplateSMSAppointment'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id AND
                             d.name IN ('expressionType', 'contentType'));

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
  WHERE e.arch_short_name = 'entity.documentTemplateSMSAppointment'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id AND
                             d.name IN ('expression', 'content'));

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
      AND a.status2 IS NULL;

UPDATE acts a
  JOIN document_acts d
    ON a.act_id = d.document_act_id
SET a.status2 = 'REVIEWED'
WHERE a.arch_short_name = 'act.patientInvestigation'
      AND a.status = 'CANCELLED'
      AND a.status2 IS NULL;

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
      AND a.status2 IS NULL;


# Migrate the act.patient*Version acts to set the status node
UPDATE acts
SET status = 'IN_PROGRESS'
WHERE arch_short_name IN
      ('act.patientDocumentAttachmentVersion', 'act.patientDocumentImageVersion', 'act.patientInvestigationVersion',
       'act.patientDocumentLetterVersion')
      AND status IS NULL;

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
  JOIN entities e ON expressionType.entity_id = e.entity_id AND expressionType.name = 'expressionType'
                     AND e.arch_short_name = 'entity.documentTemplateSMSAppointment'
SET expressionType.name = 'contentType';

UPDATE entity_details expression
  JOIN entities e ON expression.entity_id = e.entity_id AND expression.name = 'expression'
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

# update existing authorities
UPDATE granted_authorities
SET name      = 'Calendar Event Series Create',
  description = 'Authority to Create Calendar Event Series',
  archetype   = 'act.calendarEventSeries'
WHERE archetype = 'act.customerAppointmentSeries' AND method = 'create';

UPDATE granted_authorities
SET name      = 'Calendar Event Series Save',
  description = 'Authority to Save Calendar Event Series',
  archetype   = 'act.calendarEventSeries'
WHERE archetype = 'act.customerAppointmentSeries' AND method = 'save';

UPDATE granted_authorities
SET name      = 'Calendar Event Series Remove',
  description = 'Authority to Remove Calendar Event Series',
  archetype   = 'act.calendarEventSeries'
WHERE archetype = 'act.customerAppointmentSeries' AND method = 'remove';

DROP TABLE IF EXISTS new_authorities;
CREATE TEMPORARY TABLE new_authorities (
  name        VARCHAR(255) PRIMARY KEY,
  description VARCHAR(255),
  method      VARCHAR(255),
  archetype   VARCHAR(255)
);

# create new authorities
INSERT INTO new_authorities (name, description, method, archetype)
VALUES ('Calendar Block Create', 'Authority to Create Calendar Block', 'create', 'act.calendarBlock'),
  ('Calendar Block Save', 'Authority to Save Calendar Block', 'save', 'act.calendarBlock'),
  ('Calendar Block Remove', 'Authority to Remove Calendar Block', 'remove', 'act.calendarBlock'),
  ('Calendar Event Series Create', 'Authority to Create Calendar Event Series', 'create', 'act.calendarEventSeries'),
  ('Calendar Event Series Save', 'Authority to Save Calendar Event Series', 'save', 'act.calendarEventSeries'),
  ('Calendar Event Series Remove', 'Authority to Remove Calendar Event Series', 'remove', 'act.calendarEventSeries');

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

#
# OVPMS-1773 Add support to disable appointment reminders by appointment type
#

# For sites that deployed the earlier versions, default appointment types to send reminders where their schedule
# has been configured to send reminders
INSERT INTO entity_details (entity_id, name, type, value)
  SELECT DISTINCT
    appointmentType.entity_id,
    'sendReminders',
    'boolean',
    'true'
  FROM entities schedule
    JOIN entity_details sendReminders
      ON sendReminders.entity_id = schedule.entity_id AND sendReminders.name = 'sendReminders'
         AND sendReminders.value = 'true'
    JOIN entity_relationships r
      ON schedule.entity_id = r.source_id
         AND r.arch_short_name = 'entityRelationship.scheduleAppointmentType'
    JOIN entities appointmentType
      ON r.target_id = appointmentType.entity_id
  WHERE NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = appointmentType.entity_id
                         AND d.name = 'sendReminders');

#
# OVPMS-1770 Tax-exclusive product prices
#
DROP TABLE IF EXISTS product_tax_rates;

CREATE TABLE product_tax_rates (
  product_id BIGINT(20) PRIMARY KEY,
  rate       DECIMAL(18, 3)
);

INSERT INTO product_tax_rates (product_id, rate)
  SELECT
    p.product_id,
    sum(cast(rate.value AS DECIMAL(18, 3)))
  FROM products p
    JOIN entities e
      ON p.product_id = e.entity_id
    JOIN entity_classifications taxes
      ON e.entity_id = taxes.entity_id
    JOIN lookups tax
      ON tax.lookup_id = taxes.lookup_id
         AND tax.arch_short_name = 'lookup.taxType'
    JOIN lookup_details rate
      ON rate.lookup_id = tax.lookup_id
         AND rate.name = 'rate'
  WHERE NOT exists(SELECT *
                   FROM entities practice
                     JOIN entity_details d
                       ON d.entity_id = practice.entity_id
                          AND practice.arch_short_name = 'party.organisationPractice'
                          AND practice.active = 1
                          AND d.name = 'showPricesTaxInclusive')
  GROUP BY e.entity_id;

INSERT INTO product_tax_rates (product_id, rate)
  SELECT
    p.product_id,
    sum(cast(rate.value AS DECIMAL(18, 3)))
  FROM products p
    JOIN entities ep ON p.product_id = ep.entity_id
    JOIN entity_links r
      ON p.product_id = r.source_id
         AND r.arch_short_name = 'entityLink.productType'
    JOIN entities ptype
      ON ptype.entity_id = r.target_id AND ptype.active = 1
    JOIN entity_classifications taxes
      ON ptype.entity_id = taxes.entity_id
    JOIN lookups tax
      ON tax.lookup_id = taxes.lookup_id
         AND tax.arch_short_name = 'lookup.taxType'
    JOIN lookup_details rate
      ON rate.lookup_id = tax.lookup_id AND rate.name = 'rate'
  WHERE NOT exists(SELECT *
                   FROM product_tax_rates t
                   WHERE t.product_id = p.product_id)
        AND NOT exists(SELECT *
                       FROM entities practice
                         JOIN entity_details d
                           ON d.entity_id = practice.entity_id
                              AND practice.arch_short_name = 'party.organisationPractice'
                              AND practice.active = 1
                              AND d.name = 'showPricesTaxInclusive')
  GROUP BY p.product_id;


INSERT INTO product_tax_rates (product_id, rate)
  SELECT
    rates.product_id,
    rates.rate
  FROM (
         SELECT
           p.product_id,
           (SELECT sum(cast(rate.value AS DECIMAL(18, 3)))
            FROM entities o
              JOIN entity_classifications taxes
                ON o.entity_id = taxes.entity_id
              JOIN lookups tax
                ON tax.lookup_id = taxes.lookup_id
                   AND tax.arch_short_name = 'lookup.taxType'
              JOIN lookup_details rate
                ON rate.lookup_id = tax.lookup_id AND rate.name = 'rate'
            WHERE o.arch_short_name = 'party.organisationPractice' AND o.active = 1) rate
         FROM products p
         WHERE NOT exists(SELECT *
                          FROM product_tax_rates t
                          WHERE t.product_id = p.product_id)
               AND NOT exists(SELECT *
                              FROM entities practice
                                JOIN entity_details d
                                  ON d.entity_id = practice.entity_id
                                     AND practice.arch_short_name = 'party.organisationPractice'
                                     AND practice.active = 1
                                     AND d.name = 'showPricesTaxInclusive')
       ) rates
  WHERE rate IS NOT NULL AND rate > 0;

#
# Update prices.
#
UPDATE product_prices pp
  JOIN product_tax_rates rates
    ON pp.product_id = rates.product_id
SET pp.price = round(pp.price / (1 + rates.rate / 100), 3);

#
# Update markups.
#
UPDATE product_price_details d
  JOIN (
         SELECT
           pp.product_price_id,
           (round(pp.price / cast(cost.value AS DECIMAL(18, 3)), 3) - 1) * 100 markup
         FROM product_price_details markup
           JOIN product_prices pp
             ON pp.product_price_id = markup.product_price_id
                AND markup.name = 'markup'
           JOIN product_price_details cost
             ON pp.product_price_id = cost.product_price_id
                AND cost.name = 'cost'
           JOIN product_tax_rates rates
             ON pp.product_id = rates.product_id
         WHERE cast(cost.value AS DECIMAL(18, 3)) <> 0) markups
    ON d.product_price_id = markups.product_price_id
       AND markups.markup > 0
       AND d.name = 'markup'
SET d.value = markups.markup;

#
# Update max discounts.
#
UPDATE product_price_details d
  JOIN (
         SELECT
           product_price_id,
           markup,
           round((markup / (100 + markup)) * 100, 2) newMaxDiscount,
           maxDiscount
         FROM (
                SELECT
                  p.product_price_id,
                  cast(markup.value AS DECIMAL(18, 3)) markup,
                  maxDiscount.value                    maxDiscount
                FROM product_prices p
                  JOIN product_price_details markup
                    ON p.product_price_id = markup.product_price_id
                       AND markup.name = 'markup'
                  JOIN product_price_details maxDiscount
                    ON p.product_price_id = maxDiscount.product_price_id
                       AND maxDiscount.name = 'maxDiscount '
                  JOIN product_tax_rates rates
                    ON p.product_id = rates.product_id
                WHERE cast(markup.value AS DECIMAL(18, 3)) <> 0) markups) calcs
    ON calcs.product_price_id = d.product_price_id AND d.name = 'maxDiscount'
       AND maxDiscount > newMaxDiscount
SET d.value = calcs.newMaxDiscount;

UPDATE product_prices p
  JOIN product_tax_rates rates
    ON p.product_id = rates.product_id
  LEFT JOIN product_price_details uom
    ON p.product_price_id = uom.product_price_id
       AND uom.name = "qtyUom"
  LEFT JOIN lookups l
    ON l.code = uom.value AND l.arch_short_name = 'lookup.uom'
SET p.description = cast(concat('$ ', round(p.price, 2),
                                if(l.name IS NULL, '', concat(' ', l.name)),
                                ' (',
                                date_format(p.start_time, '%d/%m/%y'), ' - ',
                                if(p.end_time IS NULL, '', date_format(p.end_time, '%d/%m/%y')), ')') AS CHAR);
#
# Insert the showPricesTaxInclusive flag. This prevents subseqent migration of prices if the script is run multiple
# times.
#
INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    p.entity_id,
    'showPricesTaxInclusive',
    'boolean',
    'true'
  FROM entities p
  WHERE p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1 AND
        NOT exists(SELECT *
                   FROM entity_details d
                   WHERE d.entity_id = p.entity_id AND d.name = 'showPricesTaxInclusive');

DROP TABLE product_tax_rates;

#
# OVPMS-1783 User preferences
#

#
# Create default preferences for the practice.
#
INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.preferences',
    '1.0',
    'Preferences',
    1
  FROM entities u
  WHERE u.arch_short_name = 'party.organisationPractice' AND u.active = 1
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.arch_short_name = 'entityLink.preferencesUser' AND l.target_id = u.entity_id);

INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    u.linkId,
    'entityLink.preferencesUser',
    '1.0',
    'Preferences User',
    NULL,
    NULL,
    NULL,
    0,
    p.entity_id,
    u.entity_id
  FROM entities p
    JOIN entities u
      ON u.arch_short_name = 'party.organisationPractice'
         AND p.arch_short_name = 'entity.preferences'
         AND u.linkId = p.linkId
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = p.entity_id
            AND l.target_id = u.entity_id
            AND l.arch_short_name = 'entityLink.preferencesUser');

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.preferenceGroupSummary',
    '1.0',
    'Summary Preferences',
    1
  FROM entities p
  WHERE p.arch_short_name = 'entity.preferences'
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.arch_short_name = 'entityLink.preferenceGroupSummary' AND l.source_id = p.entity_id);

INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    p.linkId,
    'entityLink.preferenceGroupSummary',
    '1.0',
    'Summary Preferences',
    NULL,
    NULL,
    NULL,
    10,
    p.entity_id,
    c.entity_id
  FROM entities p
    JOIN entities c
      ON p.arch_short_name = 'entity.preferences'
         AND c.arch_short_name = 'entity.preferenceGroupSummary'
         AND p.linkId = c.linkId
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = p.entity_id AND l.target_id = c.entity_id AND
            l.arch_short_name = 'entityLink.preferenceGroupSummary');


INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.preferenceGroupCharge',
    '1.0',
    'Charge Preferences',
    1
  FROM entities p
  WHERE p.arch_short_name = 'entity.preferences'
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.arch_short_name = 'entityLink.preferenceGroupCharge' AND l.source_id = p.entity_id);

INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    p.linkId,
    'entityLink.preferenceGroupCharge',
    '1.0',
    'Charge Preferences',
    NULL,
    NULL,
    NULL,
    20,
    p.entity_id,
    c.entity_id
  FROM entities p
    JOIN entities c
      ON p.arch_short_name = 'entity.preferences'
         AND c.arch_short_name = 'entity.preferenceGroupCharge'
         AND p.linkId = c.linkId
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = p.entity_id AND l.target_id = c.entity_id AND
            l.arch_short_name = 'entityLink.preferenceGroupCharge');

INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    linkId,
    'entity.preferenceGroupHistory',
    '1.0',
    'Patient History Preferences',
    1
  FROM entities p
  WHERE p.arch_short_name = 'entity.preferences'
        AND NOT exists(SELECT *
                       FROM entity_links l
                       WHERE l.arch_short_name = 'entityLink.preferenceGroupHistory' AND l.source_id = p.entity_id);

INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                          active_end_time, sequence, source_id, target_id)
  SELECT
    0,
    p.linkId,
    'entityLink.preferenceGroupHistory',
    '1.0',
    'Patient History Preferences',
    NULL,
    NULL,
    NULL,
    30,
    p.entity_id,
    c.entity_id
  FROM entities p
    JOIN entities c
      ON p.arch_short_name = 'entity.preferences'
         AND c.arch_short_name = 'entity.preferenceGroupHistory'
         AND p.linkId = c.linkId
  WHERE NOT exists(
      SELECT *
      FROM entity_links l
      WHERE l.source_id = p.entity_id AND l.target_id = c.entity_id AND
            l.arch_short_name = 'entityLink.preferenceGroupHistory');

#
# Migrate preferences from the practice.
#
INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'showCustomerAccount',
    'boolean',
    ifnull(d.value, 'true')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'showCustomerAccountSummary'
  WHERE c.arch_short_name = 'entity.preferenceGroupSummary'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'showCustomerAccount');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'showReferral',
    'string',
    ifnull(d.value, 'ACTIVE')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'showReferrals'
  WHERE c.arch_short_name = 'entity.preferenceGroupSummary'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'showReferral');


INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'showProductType',
    'boolean',
    ifnull(d.value, 'false')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'showProductTypeDuringCharging'
  WHERE c.arch_short_name = 'entity.preferenceGroupCharge'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'showProductType');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'showTemplate',
    'boolean',
    ifnull(d.value, 'false')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'showTemplateDuringCharging'
  WHERE c.arch_short_name = 'entity.preferenceGroupCharge'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'showTemplate');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'showBatch',
    'boolean',
    ifnull(d.value, 'false')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'showBatchDuringCharging'
  WHERE c.arch_short_name = 'entity.preferenceGroupCharge'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'showBatch');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'showClinician',
    'boolean',
    ifnull(d.value, 'false')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'showClinicianInHistoryItems'
  WHERE c.arch_short_name = 'entity.preferenceGroupHistory'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'showClinician');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    c.entity_id,
    'sort',
    'string',
    ifnull(d.value, 'DESC')
  FROM entities c
    JOIN entities p
    LEFT JOIN entity_details d
      ON p.entity_id = d.entity_id
         AND d.name = 'medicalRecordsSortOrder'
  WHERE c.arch_short_name = 'entity.preferenceGroupHistory'
        AND p.arch_short_name = 'party.organisationPractice'
        AND p.active = 1
        AND NOT exists(SELECT *
                       FROM entity_details x
                       WHERE x.entity_id = c.entity_id AND x.name = 'sort');

#
# Delete practice preferences.
#
DELETE d
FROM entity_details d
  JOIN entities p ON p.entity_id = d.entity_id AND p.arch_short_name = 'party.organisationPractice'
WHERE d.name IN ('showCustomerAccountSummary', 'showReferrals', 'showProductTypeDuringCharging',
                 'showTemplateDuringCharging', 'showBatchDuringCharging', 'showClinicianInHistoryItems',
                 'medicalRecordsSortOrder');

#
# Security authorities
#
DROP TABLE IF EXISTS new_authorities;
CREATE TEMPORARY TABLE new_authorities (
  name        VARCHAR(255) PRIMARY KEY,
  description VARCHAR(255),
  method      VARCHAR(255),
  archetype   VARCHAR(255)
);

INSERT INTO new_authorities (name, description, method, archetype)
VALUES ('Preferences Entity Create', 'Authority to Create Preferences', 'create', 'entity.preference*'),
  ('Preferences Entity Save', 'Authority to Save Preferences', 'save', 'entity.preference*'),
  ('Preferences Entity Remove', 'Authority to Remove Preferences', 'remove', 'entity.preference*');


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
# OVPMS-1787 Product location filter.
#
INSERT INTO entity_link_details (id, type, value, name)
  SELECT
    l.id,
    'boolean',
    'false',
    'skipIfMissing'
  FROM entity_links l
  WHERE l.arch_short_name = 'entityLink.productIncludes'
        AND NOT exists(SELECT *
                       FROM entity_link_details d
                       WHERE d.id = l.id
                             AND d.name = 'skipIfMissing');

#
# OVPMS-1759 Separate mail templates from pre-fill templates
#
UPDATE entities e
SET e.arch_short_name = 'entity.documentTemplateEmailSystem'
WHERE e.arch_short_name = 'entity.documentTemplateEmail'
      AND exists(SELECT *
                 FROM entity_links l
                 WHERE l.target_id = e.entity_id AND l.arch_short_name = 'entityLink.documentTemplateEmail');

UPDATE entities e
SET e.arch_short_name = 'entity.documentTemplateEmailUser'
WHERE e.arch_short_name = 'entity.documentTemplateEmail';

#
# OVPMS-1789 Standardise security.user name attributes, and provide reporting support
#
DROP TABLE IF EXISTS new_lookups;
CREATE TEMPORARY TABLE new_lookups (
  code        VARCHAR(255) PRIMARY KEY,
  name        VARCHAR(255),
  description VARCHAR(255),
  expression  VARCHAR(5000)
);

INSERT INTO new_lookups (code, name, description, expression)
VALUES ('NAME', 'Name', 'User name format including the name only',
        'expr:ifempty(expr:concatIf($firstName, '' '', $lastName), $name)'),
  ('TITLE_NAME', 'Title and Name', 'User name format including the title and name',
   'concat(expr:concatIf($title,'' ''),
                        expr:ifempty(expr:concatIf($firstName, '' '', $lastName), $name))'),
  ('TITLE_NAME_QUALIFICATIONS', 'Title, Name and Qualifications',
   'User name format including the title, name, and qualifications',
   'concat(expr:concatIf($title,'' ''),
                             expr:ifempty(expr:concatIf($firstName, '' '', $lastName), $name),
                             expr:concatIf('', '', $qualifications))'),
  ('LOGIN_NAME', 'Login name', 'User name format including the login name', '$username'),
  ('DESCRIPTION', 'Description', 'User name format including the description', 'expr:ifempty($description, $name)');

INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.userNameFormat',
    1,
    '1.0',
    code,
    name,
    description,
    0
  FROM new_lookups l
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.userNameFormat' AND e.code = l.code);

INSERT INTO lookup_details (lookup_id, type, value, name)
  SELECT
    l.lookup_id,
    'string',
    n.expression,
    'expression'
  FROM lookups l
    JOIN new_lookups n
      ON l.code = n.code
         AND l.arch_short_name = 'lookup.userNameFormat'
         AND NOT exists(SELECT *
                        FROM lookup_details d
                        WHERE d.lookup_id = l.lookup_id
                              AND d.name = 'expression');
DROP TABLE new_lookups;

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'shortUserNameFormat',
    'string',
    'NAME'
  FROM entities e
  WHERE e.arch_short_name = 'party.organisationPractice'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id AND d.name = 'shortUserNameFormat');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'mediumUserNameFormat',
    'string',
    'TITLE_NAME'
  FROM entities e
  WHERE e.arch_short_name = 'party.organisationPractice'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id AND d.name = 'mediumUserNameFormat');

INSERT INTO entity_details (entity_id, name, type, value)
  SELECT
    e.entity_id,
    'longUserNameFormat',
    'string',
    'TITLE_NAME_QUALIFICATIONS'
  FROM entities e
  WHERE e.arch_short_name = 'party.organisationPractice'
        AND NOT exists(SELECT *
                       FROM entity_details d
                       WHERE d.entity_id = e.entity_id AND d.name = 'longUserNameFormat');
