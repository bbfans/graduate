-- MySQL dump 10.13  Distrib 5.7.11, for osx10.11 (x86_64)
--
-- Host: localhost    Database: graduate
-- ------------------------------------------------------
-- Server version	5.7.11

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
-- Table structure for table `zh_building`
--

DROP TABLE IF EXISTS `zh_building`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zh_building` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `area_id` bigint(20) NOT NULL,
  `building_kind_id` bigint(20) NOT NULL,
  `acreage` int(11) DEFAULT NULL,
  `completion_date` datetime(6) DEFAULT NULL,
  `house_num` int(11) DEFAULT NULL,
  `selled_num` int(11) DEFAULT NULL,
  `order_num` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `img` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_zh_building_area_1` (`area_id`),
  KEY `ix_zh_building_buildingKind_2` (`building_kind_id`),
  CONSTRAINT `fk_zh_building_area_1` FOREIGN KEY (`area_id`) REFERENCES `zh_area` (`id`),
  CONSTRAINT `fk_zh_building_buildingKind_2` FOREIGN KEY (`building_kind_id`) REFERENCES `zh_building_kind` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zh_building`
--

LOCK TABLES `zh_building` WRITE;
/*!40000 ALTER TABLE `zh_building` DISABLE KEYS */;
INSERT INTO `zh_building` VALUES (13,1,1,500,NULL,3,2,1,'万业湖墅金典规划为滨河居住区，斜港路南侧地块为规划中的文化娱乐用地，湖墅路东侧规划有城市绿地公园、教育配套设施以及社区公共设施。靠近轻轨2号线，站点是通达路站。','building_1.jpg'),(14,1,1,500,NULL,4,2,0,'万业湖墅金典规划为滨河居住区，斜港路南侧地块为规划中的文化娱乐用地，湖墅路东侧规划有城市绿地公园、教育配套设施以及社区公共设施。靠近轻轨2号线，站点是通达路站。','building_1.jpg'),(15,1,1,600,NULL,3,1,1,'万业湖墅金典规划为滨河居住区，斜港路南侧地块为规划中的文化娱乐用地，湖墅路东侧规划有城市绿地公园、教育配套设施以及社区公共设施。靠近轻轨2号线，站点是通达路站。','building_2.jpg'),(16,2,4,1000,NULL,1,1,0,'万业湖墅金典规划为滨河居住区，斜港路南侧地块为规划中的文化娱乐用地，湖墅路东侧规划有城市绿地公园、教育配套设施以及社区公共设施。靠近轻轨2号线，站点是通达路站。','building_2.jpg'),(17,2,4,800,NULL,1,1,0,'万业湖墅金典规划为滨河居住区，斜港路南侧地块为规划中的文化娱乐用地，湖墅路东侧规划有城市绿地公园、教育配套设施以及社区公共设施。靠近轻轨2号线，站点是通达路站。','building_2.jpg'),(18,2,4,900,NULL,1,1,0,'万业湖墅金典规划为滨河居住区，斜港路南侧地块为规划中的文化娱乐用地，湖墅路东侧规划有城市绿地公园、教育配套设施以及社区公共设施。靠近轻轨2号线，站点是通达路站。','building_2.jpg');
/*!40000 ALTER TABLE `zh_building` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-17 20:55:56