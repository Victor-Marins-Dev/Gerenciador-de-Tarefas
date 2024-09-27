DROP TABLE IF EXISTS `task_tags`;

CREATE TABLE `task_tags` (
  `task_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`task_id`,`tag_id`),
  KEY `FKeiqe3k9ent7icelm1cihqn164` (`tag_id`),
  CONSTRAINT `FK7xi1reghkj37gqwlr1ujxrxll` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FKeiqe3k9ent7icelm1cihqn164` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
