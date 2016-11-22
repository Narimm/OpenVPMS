DROP TABLE IF EXISTS OVPMS_1821;
CREATE TEMPORARY TABLE OVPMS_1821 (
  code        VARCHAR(255) PRIMARY KEY,
  name        VARCHAR(255),
  restricted  VARCHAR(255)
);

INSERT INTO OVPMS_1821 (code, name, restricted)
VALUES ('S1', 'S1', 'false'),
       ('S2', 'S2', 'false'),
       ('S3', 'S3', 'false'),
       ('S4', 'S4', 'true'),
       ('S5', 'S5', 'false'),
       ('S6', 'S6', 'false'),
       ('S7', 'S7', 'false'),
       ('S8', 'S8', 'false');

INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.productDrugSchedule',
    1,
    '1.0',
    code,
    name,
    concat(name, ' Schedule'),
    0
  FROM OVPMS_1821 l
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.productDrugSchedule' AND e.code = l.code);

INSERT INTO lookup_details (lookup_id, type, value, name)
  SELECT
    l.lookup_id,
    'boolean',
    n.restricted,
    'restricted'
  FROM lookups l
    JOIN OVPMS_1821 n
      ON l.code = n.code
         AND l.arch_short_name = 'lookup.productDrugSchedule'
         AND NOT exists(SELECT *
                        FROM lookup_details d
                        WHERE d.lookup_id = l.lookup_id
                              AND d.name = 'restricted');
DROP TABLE OVPMS_1821;

