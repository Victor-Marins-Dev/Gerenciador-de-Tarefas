DROP TABLE IF EXISTS `subtasks`;

CREATE TABLE `subtasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `task_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsvs126nsj9ohhvwjog5ddp76x` (`task_id`),
  CONSTRAINT `FKsvs126nsj9ohhvwjog5ddp76x` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `subtasks_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;