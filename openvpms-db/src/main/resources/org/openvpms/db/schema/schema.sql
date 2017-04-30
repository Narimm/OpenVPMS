-- MySQL dump 10.13  Distrib 5.1.73, for Win64 (unknown)
--
-- Host: localhost    Database: openvpms-1_8
-- ------------------------------------------------------
-- Server version	5.1.73-community-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `act_details`
--

DROP TABLE IF EXISTS `act_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `act_details` (
  `act_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`act_id`,`name`),
  KEY `FKFB795F95D8B907FA` (`act_id`),
  CONSTRAINT `FKFB795F95D8B907FA` FOREIGN KEY (`act_id`) REFERENCES `acts` (`act_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `act_identities`
--

DROP TABLE IF EXISTS `act_identities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `act_identities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `act_id` bigint(20) DEFAULT NULL,
  `arch_short_name` varchar(50) NOT NULL,
  `arch_version` varchar(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `identity` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `act_identity_idx` (`arch_short_name`,`identity`),
  KEY `FK2EA137A9D8B907FA` (`act_id`),
  CONSTRAINT `FK2EA137A9D8B907FA` FOREIGN KEY (`act_id`) REFERENCES `acts` (`act_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `act_identity_details`
--

DROP TABLE IF EXISTS `act_identity_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `act_identity_details` (
  `id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  KEY `FKC3054BCE13C93C8B` (`id`),
  CONSTRAINT `FKC3054BCE13C93C8B` FOREIGN KEY (`id`) REFERENCES `act_identities` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `act_relationship_details`
--

DROP TABLE IF EXISTS `act_relationship_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `act_relationship_details` (
  `act_relationship_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`act_relationship_id`,`name`),
  KEY `FKFF1068C87D8180BF` (`act_relationship_id`),
  CONSTRAINT `FKFF1068C87D8180BF` FOREIGN KEY (`act_relationship_id`) REFERENCES `act_relationships` (`act_relationship_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `act_relationships`
--

DROP TABLE IF EXISTS `act_relationships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `act_relationships` (
  `act_relationship_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `parent_child_relationship` bit(1) DEFAULT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`act_relationship_id`),
  KEY `FK70AA344EFCECFA9B` (`target_id`),
  KEY `FK70AA344EC84DFFD1` (`source_id`),
  CONSTRAINT `FK70AA344EC84DFFD1` FOREIGN KEY (`source_id`) REFERENCES `acts` (`act_id`) ON DELETE CASCADE,
  CONSTRAINT `FK70AA344EFCECFA9B` FOREIGN KEY (`target_id`) REFERENCES `acts` (`act_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `action_type_descriptors`
--

DROP TABLE IF EXISTS `action_type_descriptors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_type_descriptors` (
  `action_type_desc_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `class_name` varchar(255) DEFAULT NULL,
  `method_name` varchar(255) DEFAULT NULL,
  `assertion_type_desc_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`action_type_desc_id`),
  KEY `FK7974D848936158C3` (`assertion_type_desc_id`),
  CONSTRAINT `FK7974D848936158C3` FOREIGN KEY (`assertion_type_desc_id`) REFERENCES `assertion_type_descriptors` (`assertion_type_desc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acts`
--

DROP TABLE IF EXISTS `acts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `acts` (
  `act_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `activity_start_time` datetime DEFAULT NULL,
  `activity_end_time` datetime DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `status2` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`act_id`),
  KEY `act_short_name_status_idx` (`arch_short_name`,`status`),
  KEY `act_start_time_idx` (`activity_start_time`),
  KEY `act_short_name_status2_idx` (`arch_short_name`,`status2`),
  KEY `act_end_time_idx` (`activity_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `archetype_descriptors`
--

DROP TABLE IF EXISTS `archetype_descriptors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archetype_descriptors` (
  `archetype_desc_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `last_modified` datetime DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `class_name` varchar(255) NOT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `is_latest` bit(1) DEFAULT NULL,
  `is_primary` bit(1) DEFAULT NULL,
  PRIMARY KEY (`archetype_desc_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `assertion_descriptors`
--

DROP TABLE IF EXISTS `assertion_descriptors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `assertion_descriptors` (
  `assertion_desc_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `assertion_index` int(11) DEFAULT NULL,
  `error_message` varchar(255) DEFAULT NULL,
  `property_map` longtext,
  `node_desc_id` bigint(20) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`assertion_desc_id`),
  KEY `FKA1159D27273C243C` (`node_desc_id`),
  CONSTRAINT `FKA1159D27273C243C` FOREIGN KEY (`node_desc_id`) REFERENCES `node_descriptors` (`node_desc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `assertion_type_descriptors`
--

DROP TABLE IF EXISTS `assertion_type_descriptors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `assertion_type_descriptors` (
  `assertion_type_desc_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `property_archetype` varchar(255) NOT NULL,
  PRIMARY KEY (`assertion_type_desc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audit_records`
--

DROP TABLE IF EXISTS `audit_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audit_records` (
  `audit_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `archetype_id` varchar(255) DEFAULT NULL,
  `time_stamp` datetime DEFAULT NULL,
  `object_id` bigint(20) DEFAULT NULL,
  `service` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`audit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contact_classifications`
--

DROP TABLE IF EXISTS `contact_classifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contact_classifications` (
  `contact_id` bigint(20) NOT NULL,
  `lookup_id` bigint(20) NOT NULL,
  PRIMARY KEY (`contact_id`,`lookup_id`),
  KEY `FK5AC8832E6AE6B6CA` (`lookup_id`),
  KEY `FK5AC8832E3B64C74E` (`contact_id`),
  CONSTRAINT `FK5AC8832E3B64C74E` FOREIGN KEY (`contact_id`) REFERENCES `contacts` (`contact_id`),
  CONSTRAINT `FK5AC8832E6AE6B6CA` FOREIGN KEY (`lookup_id`) REFERENCES `lookups` (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contact_details`
--

DROP TABLE IF EXISTS `contact_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contact_details` (
  `contact_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`contact_id`,`name`),
  KEY `FKA3499D233B64C74E` (`contact_id`),
  CONSTRAINT `FKA3499D233B64C74E` FOREIGN KEY (`contact_id`) REFERENCES `contacts` (`contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contacts` (
  `contact_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `party_id` bigint(20) DEFAULT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `active_start_time` date DEFAULT NULL,
  `active_end_time` date DEFAULT NULL,
  PRIMARY KEY (`contact_id`),
  KEY `FKDE2D60537649DB4E` (`party_id`),
  CONSTRAINT `FKDE2D60537649DB4E` FOREIGN KEY (`party_id`) REFERENCES `parties` (`party_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_acts`
--

DROP TABLE IF EXISTS `document_acts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_acts` (
  `document_act_id` bigint(20) NOT NULL,
  `doc_version` varchar(255) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `mime_type` varchar(255) DEFAULT NULL,
  `printed` bit(1) DEFAULT NULL,
  `document_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`document_act_id`),
  KEY `FK5E78FAC5D1AECF9E` (`document_act_id`),
  KEY `FK5E78FAC592203729` (`document_id`),
  CONSTRAINT `FK5E78FAC592203729` FOREIGN KEY (`document_id`) REFERENCES `documents` (`document_id`),
  CONSTRAINT `FK5E78FAC5D1AECF9E` FOREIGN KEY (`document_act_id`) REFERENCES `acts` (`act_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_details`
--

DROP TABLE IF EXISTS `document_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_details` (
  `document_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`document_id`,`name`),
  KEY `FK829C1F1E92203729` (`document_id`),
  CONSTRAINT `FK829C1F1E92203729` FOREIGN KEY (`document_id`) REFERENCES `documents` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `documents`
--

DROP TABLE IF EXISTS `documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documents` (
  `document_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `mime_type` varchar(255) DEFAULT NULL,
  `doc_size` int(11) DEFAULT NULL,
  `checksum` bigint(20) DEFAULT NULL,
  `contents` longblob,
  PRIMARY KEY (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entities`
--

DROP TABLE IF EXISTS `entities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entities` (
  `entity_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  PRIMARY KEY (`entity_id`),
  KEY `entity_arch_sn_name_idx` (`arch_short_name`,`name`),
  KEY `entity_name_idx` (`name`),
  KEY `entity_short_name_idx` (`arch_short_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_classifications`
--

DROP TABLE IF EXISTS `entity_classifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_classifications` (
  `entity_id` bigint(20) NOT NULL,
  `lookup_id` bigint(20) NOT NULL,
  PRIMARY KEY (`entity_id`,`lookup_id`),
  KEY `FK76B55CF16AE6B6CA` (`lookup_id`),
  KEY `FK76B55CF14372B7A1` (`entity_id`),
  CONSTRAINT `FK76B55CF14372B7A1` FOREIGN KEY (`entity_id`) REFERENCES `entities` (`entity_id`),
  CONSTRAINT `FK76B55CF16AE6B6CA` FOREIGN KEY (`lookup_id`) REFERENCES `lookups` (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_details`
--

DROP TABLE IF EXISTS `entity_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_details` (
  `entity_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`entity_id`,`name`),
  KEY `FKD621E9E64372B7A1` (`entity_id`),
  CONSTRAINT `FKD621E9E64372B7A1` FOREIGN KEY (`entity_id`) REFERENCES `entities` (`entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_identities`
--

DROP TABLE IF EXISTS `entity_identities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_identities` (
  `entity_identity_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `identity` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`entity_identity_id`),
  KEY `entity_identity_name_idx` (`name`),
  KEY `FKB1D93FB84372B7A1` (`entity_id`),
  CONSTRAINT `FKB1D93FB84372B7A1` FOREIGN KEY (`entity_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_identity_details`
--

DROP TABLE IF EXISTS `entity_identity_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_identity_details` (
  `entity_identity_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`entity_identity_id`,`name`),
  KEY `FK4794CC9D3C2625E8` (`entity_identity_id`),
  CONSTRAINT `FK4794CC9D3C2625E8` FOREIGN KEY (`entity_identity_id`) REFERENCES `entity_identities` (`entity_identity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_link_details`
--

DROP TABLE IF EXISTS `entity_link_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_link_details` (
  `id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  KEY `FKD2E4DDD9A03946DF` (`id`),
  CONSTRAINT `FKD2E4DDD9A03946DF` FOREIGN KEY (`id`) REFERENCES `entity_links` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_links`
--

DROP TABLE IF EXISTS `entity_links`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_links` (
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
  KEY `FK3FCDE05D3EFA2333` (`target_id`),
  KEY `FK3FCDE05DA5B2869` (`source_id`),
  CONSTRAINT `FK3FCDE05D3EFA2333` FOREIGN KEY (`target_id`) REFERENCES `entities` (`entity_id`),
  CONSTRAINT `FK3FCDE05DA5B2869` FOREIGN KEY (`source_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_relationship_details`
--

DROP TABLE IF EXISTS `entity_relationship_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_relationship_details` (
  `entity_relationship_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`entity_relationship_id`,`name`),
  KEY `FKBB44EA17AB042EA8` (`entity_relationship_id`),
  CONSTRAINT `FKBB44EA17AB042EA8` FOREIGN KEY (`entity_relationship_id`) REFERENCES `entity_relationships` (`entity_relationship_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_relationships`
--

DROP TABLE IF EXISTS `entity_relationships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_relationships` (
  `entity_relationship_id` bigint(20) NOT NULL AUTO_INCREMENT,
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
  `identity_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`entity_relationship_id`),
  KEY `FK861BFDDF529A044` (`identity_id`),
  KEY `FK861BFDDF3EFA2333` (`target_id`),
  KEY `FK861BFDDFA5B2869` (`source_id`),
  CONSTRAINT `FK861BFDDF3EFA2333` FOREIGN KEY (`target_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE,
  CONSTRAINT `FK861BFDDF529A044` FOREIGN KEY (`identity_id`) REFERENCES `entity_identities` (`entity_identity_id`),
  CONSTRAINT `FK861BFDDFA5B2869` FOREIGN KEY (`source_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `etl_log`
--

DROP TABLE IF EXISTS `etl_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `etl_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `row_id` varchar(32) DEFAULT NULL,
  `loader` varchar(32) DEFAULT NULL,
  `archetype` varchar(255) DEFAULT NULL,
  `collection_index` int(11) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `linkId` varchar(36) DEFAULT NULL,
  `error_messages` varchar(1536) DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `etl_rowId_archetype_idx` (`row_id`,`archetype`),
  KEY `etl_rowId_loader_idx` (`row_id`,`loader`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `financial_acts`
--

DROP TABLE IF EXISTS `financial_acts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `financial_acts` (
  `financial_act_id` bigint(20) NOT NULL,
  `quantity` decimal(18,3) DEFAULT NULL,
  `fixed_amount` decimal(18,3) DEFAULT NULL,
  `unit_amount` decimal(18,3) DEFAULT NULL,
  `fixed_cost` decimal(18,3) DEFAULT NULL,
  `unit_cost` decimal(18,3) DEFAULT NULL,
  `tax_amount` decimal(18,3) DEFAULT NULL,
  `total` decimal(18,3) DEFAULT NULL,
  `allocated_amount` decimal(18,3) DEFAULT NULL,
  `credit` bit(1) DEFAULT NULL,
  `printed` bit(1) DEFAULT NULL,
  PRIMARY KEY (`financial_act_id`),
  KEY `FK3D69E417B80AAC70` (`financial_act_id`),
  CONSTRAINT `FK3D69E417B80AAC70` FOREIGN KEY (`financial_act_id`) REFERENCES `acts` (`act_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `granted_authorities`
--

DROP TABLE IF EXISTS `granted_authorities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `granted_authorities` (
  `granted_authority_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `archetype` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`granted_authority_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lookup_details`
--

DROP TABLE IF EXISTS `lookup_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lookup_details` (
  `lookup_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`lookup_id`,`name`),
  KEY `FKB2E8287D6AE6B6CA` (`lookup_id`),
  CONSTRAINT `FKB2E8287D6AE6B6CA` FOREIGN KEY (`lookup_id`) REFERENCES `lookups` (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lookup_relationship_details`
--

DROP TABLE IF EXISTS `lookup_relationship_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lookup_relationship_details` (
  `lookup_relationship_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`lookup_relationship_id`,`name`),
  KEY `FK40E558E08B49F65F` (`lookup_relationship_id`),
  CONSTRAINT `FK40E558E08B49F65F` FOREIGN KEY (`lookup_relationship_id`) REFERENCES `lookup_relationships` (`lookup_relationship_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lookup_relationships`
--

DROP TABLE IF EXISTS `lookup_relationships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lookup_relationships` (
  `lookup_relationship_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`lookup_relationship_id`),
  KEY `FK2C88AF36F5B96353` (`target_id`),
  KEY `FK2C88AF36C11A6889` (`source_id`),
  CONSTRAINT `FK2C88AF36C11A6889` FOREIGN KEY (`source_id`) REFERENCES `lookups` (`lookup_id`) ON DELETE CASCADE,
  CONSTRAINT `FK2C88AF36F5B96353` FOREIGN KEY (`target_id`) REFERENCES `lookups` (`lookup_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lookups`
--

DROP TABLE IF EXISTS `lookups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lookups` (
  `lookup_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `code` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `default_lookup` bit(1) DEFAULT NULL,
  PRIMARY KEY (`lookup_id`),
  UNIQUE KEY `arch_short_name` (`arch_short_name`,`code`),
  KEY `lookup_short_name_index` (`arch_short_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `node_descriptors`
--

DROP TABLE IF EXISTS `node_descriptors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node_descriptors` (
  `node_desc_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `path` varchar(255) NOT NULL,
  `base_name` varchar(255) DEFAULT NULL,
  `default_value` varchar(5000) DEFAULT NULL,
  `filter` varchar(255) DEFAULT NULL,
  `derived_value` varchar(5000) DEFAULT NULL,
  `is_readonly` bit(1) DEFAULT NULL,
  `is_hidden` bit(1) DEFAULT NULL,
  `is_derived` bit(1) DEFAULT NULL,
  `min_cardinality` int(11) DEFAULT NULL,
  `max_cardinality` int(11) DEFAULT NULL,
  `min_length` int(11) DEFAULT NULL,
  `max_length` int(11) DEFAULT NULL,
  `node_index` int(11) DEFAULT NULL,
  `is_parent_child` bit(1) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `archetype_desc_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`node_desc_id`),
  KEY `FKAB46B427F01904C0` (`parent_id`),
  KEY `FKAB46B42775928C22` (`archetype_desc_id`),
  CONSTRAINT `FKAB46B42775928C22` FOREIGN KEY (`archetype_desc_id`) REFERENCES `archetype_descriptors` (`archetype_desc_id`) ON DELETE CASCADE,
  CONSTRAINT `FKAB46B427F01904C0` FOREIGN KEY (`parent_id`) REFERENCES `node_descriptors` (`node_desc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `participation_details`
--

DROP TABLE IF EXISTS `participation_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `participation_details` (
  `participation_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`participation_id`,`name`),
  KEY `FK64ED55446614381A` (`participation_id`),
  CONSTRAINT `FK64ED55446614381A` FOREIGN KEY (`participation_id`) REFERENCES `participations` (`participation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `participations`
--

DROP TABLE IF EXISTS `participations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `participations` (
  `participation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `act_arch_short_name` varchar(100) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `act_id` bigint(20) DEFAULT NULL,
  `activity_start_time` datetime DEFAULT NULL,
  `activity_end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`participation_id`),
  KEY `participation_arch_short_name_idx` (`arch_short_name`),
  KEY `participation_entity_end_time_idx` (`entity_id`,`activity_end_time`),
  KEY `participation_act_entity_start_time_idx` (`act_arch_short_name`,`entity_id`),
  KEY `participation_entity_start_time_idx` (`entity_id`,`activity_start_time`),
  KEY `FKA301B52D8B907FA` (`act_id`),
  KEY `FKA301B524372B7A1` (`entity_id`),
  CONSTRAINT `FKA301B524372B7A1` FOREIGN KEY (`entity_id`) REFERENCES `entities` (`entity_id`),
  CONSTRAINT `FKA301B52D8B907FA` FOREIGN KEY (`act_id`) REFERENCES `acts` (`act_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `parties`
--

DROP TABLE IF EXISTS `parties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parties` (
  `party_id` bigint(20) NOT NULL,
  PRIMARY KEY (`party_id`),
  KEY `FKD0BCCA04B67A7DBE` (`party_id`),
  CONSTRAINT `FKD0BCCA04B67A7DBE` FOREIGN KEY (`party_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_price_classifications`
--

DROP TABLE IF EXISTS `product_price_classifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_price_classifications` (
  `product_price_id` bigint(20) NOT NULL,
  `lookup_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_price_id`,`lookup_id`),
  KEY `FK9EC6BF076AE6B6CA` (`lookup_id`),
  KEY `FK9EC6BF07521C9574` (`product_price_id`),
  CONSTRAINT `FK9EC6BF07521C9574` FOREIGN KEY (`product_price_id`) REFERENCES `product_prices` (`product_price_id`),
  CONSTRAINT `FK9EC6BF076AE6B6CA` FOREIGN KEY (`lookup_id`) REFERENCES `lookups` (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_price_details`
--

DROP TABLE IF EXISTS `product_price_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_price_details` (
  `product_price_id` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`product_price_id`,`name`),
  KEY `FKF9A9C1FC521C9574` (`product_price_id`),
  CONSTRAINT `FKF9A9C1FC521C9574` FOREIGN KEY (`product_price_id`) REFERENCES `product_prices` (`product_price_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_prices`
--

DROP TABLE IF EXISTS `product_prices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_prices` (
  `product_price_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `is_fixed` bit(1) DEFAULT NULL,
  `price` decimal(18,3) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`product_price_id`),
  KEY `FKFBD40D9AE51F1FB7` (`product_id`),
  CONSTRAINT `FKFBD40D9AE51F1FB7` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `products` (
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FKC42BD164D813A315` (`product_id`),
  CONSTRAINT `FKC42BD164D813A315` FOREIGN KEY (`product_id`) REFERENCES `entities` (`entity_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles_authorities`
--

DROP TABLE IF EXISTS `roles_authorities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles_authorities` (
  `security_role_id` bigint(20) NOT NULL,
  `authority_id` bigint(20) NOT NULL,
  PRIMARY KEY (`security_role_id`,`authority_id`),
  KEY `FKE9CCCC9F2FE5EADB` (`authority_id`),
  KEY `FKE9CCCC9F844DFA25` (`security_role_id`),
  CONSTRAINT `FKE9CCCC9F2FE5EADB` FOREIGN KEY (`authority_id`) REFERENCES `granted_authorities` (`granted_authority_id`),
  CONSTRAINT `FKE9CCCC9F844DFA25` FOREIGN KEY (`security_role_id`) REFERENCES `security_roles` (`security_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security_roles`
--

DROP TABLE IF EXISTS `security_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_roles` (
  `security_role_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `linkId` varchar(36) NOT NULL,
  `arch_short_name` varchar(100) NOT NULL,
  `arch_version` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  PRIMARY KEY (`security_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `security_role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`security_role_id`),
  KEY `FK734299495F0477E4` (`user_id`),
  KEY `FK73429949844DFA25` (`security_role_id`),
  CONSTRAINT `FK734299495F0477E4` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK73429949844DFA25` FOREIGN KEY (`security_role_id`) REFERENCES `security_roles` (`security_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `user_name` varchar(20) NOT NULL,
  `password` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_name` (`user_name`),
  KEY `FK6A68E0826A12449` (`user_id`),
  CONSTRAINT `FK6A68E0826A12449` FOREIGN KEY (`user_id`) REFERENCES `parties` (`party_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-04-30  3:10:04
