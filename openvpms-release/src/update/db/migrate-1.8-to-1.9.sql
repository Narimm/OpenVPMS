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
VALUES ("Document Logo Act Create", "Authority to Create Document Logo Act", "create", "act.documentLogo"),
  ("Document Logo Act Save", "Authority to Save Document Logo Act", "save", "act.documentLogo"),
  ("Document Logo Act Remove", "Authority to Remove Document Logo Act", "remove", "act.documentLogo");

INSERT INTO granted_authorities (version, linkId, arch_short_name, arch_version, name, description, active, service_name, method, archetype)
  SELECT
    0,
    UUID(),
    'security.archetypeAuthority',
    "1.0",
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
  WHERE r.name = "Base Role" AND NOT exists
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
         AND criticalQty.name = "criticalQty"
    JOIN entity_relationship_details idealQty
      ON r.entity_relationship_id = idealQty.entity_relationship_id
         AND idealQty.name = "idealQty"
         AND idealQty.value < criticalQty.value
  WHERE r.arch_short_name = "entityRelationship.productStockLocation";

UPDATE entity_relationships r
  JOIN ideal_critical_qty i
    ON i.id = r.entity_relationship_id
  JOIN entity_relationship_details criticalQty
    ON r.entity_relationship_id = criticalQty.entity_relationship_id
       AND criticalQty.name = "criticalQty"
  JOIN entity_relationship_details idealQty
    ON r.entity_relationship_id = idealQty.entity_relationship_id
       AND idealQty.name = "idealQty"
SET idealQty.value  = i.criticalQty,
  criticalQty.value = i.idealQty;

DROP TABLE ideal_critical_qty;

