-- MySQL dump 10.13  Distrib 5.7.22, for Linux (x86_64)
--
-- Host: localhost    Database: nblog
-- ------------------------------------------------------
-- Server version	5.7.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `blog_article`
--

DROP TABLE IF EXISTS `blog_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_article` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `pubDate` datetime DEFAULT NULL,
  `lastModifyDate` datetime DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `isPrivate` tinyint(1) NOT NULL DEFAULT '0',
  `hits` int(11) NOT NULL DEFAULT '0',
  `summary` varchar(3000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `art_level` int(11) DEFAULT NULL,
  `art_status` int(11) NOT NULL DEFAULT '0',
  `art_from` int(11) NOT NULL DEFAULT '0',
  `editor` int(11) NOT NULL DEFAULT '0',
  `space_id` int(11) NOT NULL,
  `art_lock` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `art_alias` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `allowComment` tinyint(1) NOT NULL DEFAULT '1',
  `feature_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `alias_index` (`art_alias`)
) ENGINE=InnoDB AUTO_INCREMENT=568 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_article_tag`
--

DROP TABLE IF EXISTS `blog_article_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_article_tag` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `article_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3649 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_comment`
--

DROP TABLE IF EXISTS `blog_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) DEFAULT NULL,
  `parent_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `comment_nickname` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `comment_email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `comment_ip` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `comment_admin` tinyint(1) NOT NULL DEFAULT '0',
  `content` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module_id` int(11) NOT NULL,
  `comment_date` datetime NOT NULL,
  `comment_status` int(11) NOT NULL,
  `comment_website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `COMMENT_GRAVATAR` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `module_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `comment_editor` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=554 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_common_file`
--

DROP TABLE IF EXISTS `blog_common_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_common_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_extension` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` int(11) NOT NULL,
  `file_store` int(11) NOT NULL,
  `file_originalname` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_width` int(11) DEFAULT NULL,
  `file_height` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2579 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_file`
--

DROP TABLE IF EXISTS `blog_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_parent` int(11) DEFAULT NULL,
  `file_type` int(11) NOT NULL DEFAULT '0',
  `file_createDate` datetime NOT NULL,
  `common_file` int(11) DEFAULT NULL,
  `file_lft` int(11) NOT NULL,
  `file_rgt` int(11) NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2913 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_file_delete`
--

DROP TABLE IF EXISTS `blog_file_delete`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_file_delete` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_store` int(11) DEFAULT NULL,
  `file_key` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_type` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_fragment_user`
--

DROP TABLE IF EXISTS `blog_fragment_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_fragment_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fragment_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fragment_description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fragment_tpl` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fragment_create_date` datetime NOT NULL,
  `space_id` int(11) DEFAULT NULL,
  `is_global` tinyint(1) NOT NULL DEFAULT '0',
  `is_callable` tinyint(1) NOT NULL DEFAULT '0',
  `is_del` tinyint(1) NOT NULL DEFAULT '0',
  `is_enable` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=163 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_history_template`
--

DROP TABLE IF EXISTS `blog_history_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_history_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_tpl` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `template_time` datetime NOT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `template_id` int(11) NOT NULL,
  `template_type` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_lock`
--

DROP TABLE IF EXISTS `blog_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_lock` (
  `id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lock_type` int(11) NOT NULL DEFAULT '0',
  `lock_password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lock_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `createDate` datetime NOT NULL,
  `lock_question` text COLLATE utf8mb4_unicode_ci,
  `lock_answers` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_news`
--

DROP TABLE IF EXISTS `blog_news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_news` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `news_content` varchar(4000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `news_private` tinyint(1) NOT NULL DEFAULT '0',
  `news_allowComment` tinyint(1) NOT NULL DEFAULT '1',
  `news_write` datetime NOT NULL,
  `news_update` datetime DEFAULT NULL,
  `news_hits` int(11) NOT NULL DEFAULT '0',
  `news_lock` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `news_editor` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=318 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_page_user`
--

DROP TABLE IF EXISTS `blog_page_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_page_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `page_tpl` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `page_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `page_description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `space_id` int(11) DEFAULT NULL,
  `page_create_date` datetime NOT NULL,
  `page_alias` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `page_allowComment` tinyint(1) NOT NULL DEFAULT '0',
  `page_spaceGlobal` tinyint(1) NOT NULL DEFAULT '0',
  `is_enable` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=302 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_space`
--

DROP TABLE IF EXISTS `blog_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_space` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `space_alias` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `createDate` datetime NOT NULL,
  `space_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `space_lock` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_private` tinyint(1) NOT NULL DEFAULT '0',
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `space_alias` (`space_alias`),
  UNIQUE KEY `space_name` (`space_name`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blog_tag`
--

DROP TABLE IF EXISTS `blog_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_tag` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_date` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `blog_pluginTemplate`
--

DROP TABLE IF EXISTS `blog_pluginTemplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_pluginTemplate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pluginName` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `template` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-11-12 20:17:01
