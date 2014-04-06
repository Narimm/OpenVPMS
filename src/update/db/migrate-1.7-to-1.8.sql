CREATE TABLE if not exists `entity_link_details` (
  `id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  KEY `FKD2E4DDD9A03946DF` (`id`),
  CONSTRAINT `FKD2E4DDD9A03946DF` FOREIGN KEY (`id`) REFERENCES `entity_links` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE if not exists `entity_links` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active_start_time` datetime DEFAULT NULL,
  `active_end_time` datetime DEFAULT NULL,
  `sequence` int(11) NOT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3FCDE05DA5B2869` (`source_id`),
  KEY `FK3FCDE05D3EFA2333` (`target_id`),
  CONSTRAINT `FK3FCDE05D3EFA2333` FOREIGN KEY (`target_id`) REFERENCES `entities` (`entity_id`),
  CONSTRAINT `FK3FCDE05DA5B2869` FOREIGN KEY (`source_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
