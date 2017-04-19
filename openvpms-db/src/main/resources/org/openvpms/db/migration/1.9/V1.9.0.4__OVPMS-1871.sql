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
