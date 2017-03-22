#
# Add an Online Booking role.
#
INSERT INTO security_roles (version, linkId, arch_short_name, arch_version, name, description, active)
  SELECT
    0,
    UUID(),
    'security.role',
    '1.0',
    'Online Booking',
    'Restricted role to enable external providers to submit online bookings',
    1
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM security_roles
                   WHERE name = 'Online Booking');


DROP TABLE IF EXISTS tmp_roles;
CREATE TEMPORARY TABLE tmp_roles (
  granted_authority_id BIGINT PRIMARY KEY NOT NULL,
  archetype            VARCHAR(255)       NOT NULL,
  method               VARCHAR(255)       NOT NULL
);


INSERT INTO tmp_roles (granted_authority_id, archetype, method)
  SELECT
    granted_authority_id,
    archetype,
    method
  FROM granted_authorities
  WHERE arch_short_name = 'security.archetypeAuthority'
        AND method IN ('create', 'save')
        AND archetype IN ('act.customerAppointment', 'participation.*', 'actRelationship.*');

INSERT INTO roles_authorities (security_role_id, authority_id)
  SELECT
    s.security_role_id,
    r.granted_authority_id
  FROM security_roles s
    JOIN tmp_roles r
  WHERE s.name = 'Online Booking' AND NOT exists(SELECT *
                                                 FROM roles_authorities ra
                                                 WHERE ra.security_role_id = s.security_role_id AND
                                                       ra.authority_id = r.granted_authority_id);
DROP TABLE IF EXISTS tmp_roles;
