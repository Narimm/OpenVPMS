#
# Create the act_identities table.
#

CREATE TABLE IF NOT EXISTS `act_identities` (
  `id`              BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `version`         BIGINT(20)  NOT NULL,
  `linkId`          VARCHAR(36) NOT NULL,
  `act_id`          BIGINT(20)           DEFAULT NULL,
  `arch_short_name` VARCHAR(50) NOT NULL,
  `arch_version`    VARCHAR(20) NOT NULL,
  `name`            VARCHAR(255)         DEFAULT NULL,
  `description`     VARCHAR(255)         DEFAULT NULL,
  `active`          BIT(1)               DEFAULT NULL,
  `identity`        VARCHAR(100)         DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `act_identity_idx` (`arch_short_name`, `identity`),
  KEY `FK2EA137A9D8B907FA` (`act_id`),
  CONSTRAINT `FK2EA137A9D8B907FA` FOREIGN KEY (`act_id`) REFERENCES `acts` (`act_id`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

#
# Create the act_identity_details table.
#

CREATE TABLE IF NOT EXISTS `act_identity_details` (
  `id`    BIGINT(20)    NOT NULL,
  `type`  VARCHAR(255)  NOT NULL,
  `value` VARCHAR(5000) NOT NULL,
  `name`  VARCHAR(255)  NOT NULL,
  PRIMARY KEY (`id`, `name`),
  KEY `FKC3054BCE13C93C8B` (`id`),
  CONSTRAINT `FKC3054BCE13C93C8B` FOREIGN KEY (`id`) REFERENCES `act_identities` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


#
# Create a 'mg' lookup.uom
#
INSERT INTO lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
  SELECT
    0,
    UUID(),
    'lookup.uom',
    1,
    '1.0',
    'MG',
    'Mg',
    NULL,
    0
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM lookups e
                   WHERE e.arch_short_name = 'lookup.uom' AND e.code = 'MG');

INSERT INTO lookup_details (lookup_id, type, value, name)
  SELECT
    l.lookup_id,
    'string',
    'mg',
    'printedName'
  FROM lookups l
  where l.arch_short_name = 'lookup.uom'
	and l.code = 'MG'
    and NOT exists(SELECT *
                        FROM lookup_details d
                        WHERE d.lookup_id = l.lookup_id
                              AND d.name = 'printedName');

INSERT INTO lookup_details (lookup_id, type, value, name)
  SELECT
    l.lookup_id,
    'string',
    'MGM',
    'unitCode'
  FROM lookups l
  where l.arch_short_name = 'lookup.uom'
	and l.code = 'MG'
    and NOT exists(SELECT *
                        FROM lookup_details d
                        WHERE d.lookup_id = l.lookup_id
                              AND d.name = 'unitCode');
