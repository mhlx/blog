-- MariaDB dump 10.17  Distrib 10.4.8-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: blog8
-- ------------------------------------------------------
-- Server version	10.4.8-MariaDB

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
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTENT` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `PUBDATE` datetime DEFAULT NULL,
  `LASTMODIFYDATE` datetime DEFAULT NULL,
  `TITLE` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ISPRIVATE` tinyint(4) NOT NULL DEFAULT 0,
  `HITS` int(11) NOT NULL DEFAULT 0,
  `SUMMARY` varchar(3000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `LEVEL` int(11) NOT NULL,
  `PASSWORD` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ALIAS` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ALLOWCOMMENT` tinyint(4) NOT NULL DEFAULT 1,
  `STATUS` tinyint(4) NOT NULL DEFAULT 0,
  `ISDELETED` tinyint(1) NOT NULL DEFAULT 0,
  `FEATUREIMAGE` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_article`
--

LOCK TABLES `blog_article` WRITE;
/*!40000 ALTER TABLE `blog_article` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_article_category`
--

DROP TABLE IF EXISTS `blog_article_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_article_category` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ARTICLEID` int(11) NOT NULL,
  `CATEGORYID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_article_category`
--

LOCK TABLES `blog_article_category` WRITE;
/*!40000 ALTER TABLE `blog_article_category` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_article_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_article_tag`
--

DROP TABLE IF EXISTS `blog_article_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_article_tag` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ARTICLEID` int(11) NOT NULL,
  `TAGID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_article_tag`
--

LOCK TABLES `blog_article_tag` WRITE;
/*!40000 ALTER TABLE `blog_article_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_article_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_blackip`
--

DROP TABLE IF EXISTS `blog_blackip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_blackip` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `IP` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_blackip`
--

LOCK TABLES `blog_blackip` WRITE;
/*!40000 ALTER TABLE `blog_blackip` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_blackip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_category`
--

DROP TABLE IF EXISTS `blog_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_category` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CREATETIME` datetime NOT NULL,
  `MODIFYTIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_category`
--

LOCK TABLES `blog_category` WRITE;
/*!40000 ALTER TABLE `blog_category` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_comment`
--

DROP TABLE IF EXISTS `blog_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_comment` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `PARENT` int(11) DEFAULT NULL,
  `PARENTPATH` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NICKNAME` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `EMAIL` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `IP` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ADMIN` tinyint(4) NOT NULL DEFAULT 0,
  `CONTENT` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `MODULE_ID` int(11) NOT NULL,
  `CREATETIME` datetime NOT NULL,
  `MODIFYTIME` datetime DEFAULT NULL,
  `WEBSITE` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `GRAVATAR` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `MODULE_NAME` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CHECKING` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_comment`
--

LOCK TABLES `blog_comment` WRITE;
/*!40000 ALTER TABLE `blog_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_moment`
--

DROP TABLE IF EXISTS `blog_moment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_moment` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTENT` varchar(4000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ISPRIVATE` tinyint(4) NOT NULL DEFAULT 0,
  `ALLOWCOMMENT` tinyint(4) NOT NULL DEFAULT 1,
  `TIME` datetime NOT NULL,
  `MODIFYTIME` datetime DEFAULT NULL,
  `HITS` int(11) NOT NULL DEFAULT 0,
  `PASSWORD` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_moment`
--

LOCK TABLES `blog_moment` WRITE;
/*!40000 ALTER TABLE `blog_moment` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_moment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_tag`
--

DROP TABLE IF EXISTS `blog_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_tag` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CREATETIME` datetime NOT NULL,
  `MODIFYTIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_tag`
--

LOCK TABLES `blog_tag` WRITE;
/*!40000 ALTER TABLE `blog_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blog_template`
--

DROP TABLE IF EXISTS `blog_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blog_template` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTENT` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `NAME` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PATTERN` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CREATETIME` datetime NOT NULL,
  `MODIFYTIME` datetime DEFAULT NULL,
  `ENABLE` tinyint(1) NOT NULL DEFAULT 0,
  `ALLOWCOMMENT` tinyint(1) NOT NULL DEFAULT 0,
  `DESCRIPTION` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog_template`
--

LOCK TABLES `blog_template` WRITE;
/*!40000 ALTER TABLE `blog_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-12-07 15:15:05
