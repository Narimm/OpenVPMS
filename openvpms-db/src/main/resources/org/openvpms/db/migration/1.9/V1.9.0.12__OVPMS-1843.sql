#
# OVPMS-1843 Add till balance and till balance adjustments to existing installations
#

#
# This adds authorities for act.tillBalance and act.tillBalanceAdjust, and adds the create and save authorities
# to the Base Role.
#

DROP TABLE IF EXISTS new_authorities;
CREATE TEMPORARY TABLE new_authorities (
  name        VARCHAR(255) PRIMARY KEY,
  description VARCHAR(255),
  method      VARCHAR(255),
  archetype   VARCHAR(255)
);

INSERT INTO new_authorities (name, description, method, archetype)
VALUES ('Till Balance Create', 'Authority to Create Till Balance', 'create', 'act.tillBalance'),
  ('Till Balance Save', 'Authority to Save Till Balance', 'save', 'act.tillBalance'),
  ('Till Balance Remove', 'Authority to Remove Till Balance', 'remove', 'act.tillBalance'),

  ('Till Balance Adjustment Create', 'Authority to Create Till Balance Adjustment', 'create',
   'act.tillBalanceAdjustment'),
  ('Till Balance Adjustment Save', 'Authority to Save Till Balance Adjustment', 'save', 'act.tillBalanceAdjustment'),
  ('Till Balance Adjustment Remove', 'Authority to Remove Till Balance Adjustment', 'remove',
   'act.tillBalanceAdjustment');

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
# Add create and save authorities to the Base Role.
#
INSERT INTO roles_authorities (security_role_id, authority_id)
  SELECT
    r.security_role_id,
    g.granted_authority_id
  FROM security_roles r
    JOIN granted_authorities g
  WHERE r.name = 'Base Role'
        AND g.archetype IN ('act.tillBalance', 'act.tillBalanceAdjustment')
        AND g.method IN ('create', 'save')
        AND NOT exists(SELECT *
                       FROM roles_authorities x
                       WHERE x.security_role_id = r.security_role_id
                             AND x.authority_id = g.granted_authority_id);

DROP TABLE new_authorities;

