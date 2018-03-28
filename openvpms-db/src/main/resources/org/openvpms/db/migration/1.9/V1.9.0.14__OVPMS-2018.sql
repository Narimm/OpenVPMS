#
# OVPMS-2018 Add authorities for patient reminder items to Base Role
#
#
# This adds authorities for act.patientReminderItem* and adds them to Base Role
#

DROP TABLE IF EXISTS new_authorities;
CREATE TEMPORARY TABLE new_authorities (
  name        VARCHAR(255) PRIMARY KEY,
  description VARCHAR(255),
  method      VARCHAR(255),
  archetype   VARCHAR(255)
);

INSERT INTO new_authorities (name, description, method, archetype)
VALUES ('Patient Reminder Item Create', 'Authority to Create Reminder Item', 'create', 'act.patientReminderItem*'),
  ('Patient Reminder Item Save', 'Authority to Save Reminder Item', 'save', 'act.patientReminderItem*'),
  ('Patient Reminder Item Remove', 'Authority to Remove Reminder Item', 'remove', 'act.patientReminderItem*');

#
# Create authorities, if they don't already exist.
#
INSERT INTO granted_authorities (version, linkId, arch_short_name, arch_version, name, description, active,
                                 service_name, method, archetype)
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
      WHERE g.method = a.method
            AND g.archetype = a.archetype);

#
# Add authorities to Base Role.
#
INSERT INTO roles_authorities (security_role_id, authority_id)
  SELECT
    r.security_role_id,
    g.granted_authority_id
  FROM security_roles r
    JOIN granted_authorities g
  WHERE r.name = 'Base Role'
        AND g.archetype IN ('act.patientReminderItem*')
        AND g.method IN ('create', 'save', 'remove')
        AND NOT exists(SELECT *
                       FROM roles_authorities x
                       WHERE x.security_role_id = r.security_role_id
                             AND x.authority_id = g.granted_authority_id);

DROP TABLE new_authorities;

