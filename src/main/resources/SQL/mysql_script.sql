USE authtion;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE `users` (
  `id`                      VARCHAR(255)
                            COLLATE utf8mb4_unicode_ci NOT NULL,
  `password`                VARCHAR(255)
                            COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name`              VARCHAR(255)
                            COLLATE utf8mb4_unicode_ci          DEFAULT '',
  `last_name`               VARCHAR(255)
                            COLLATE utf8mb4_unicode_ci          DEFAULT '',
  `account_non_expired`     TINYINT(1)                 NOT NULL DEFAULT '1',
  `credentials_non_expired` TINYINT(1)                 NOT NULL DEFAULT '1',
  `account_non_locked`      TINYINT(1)                 NOT NULL DEFAULT '1',
  `enabled`                 TINYINT(1)                 NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_name_uindex` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `authorities`;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE `authorities` (
  `authority`   VARCHAR(255)
                COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` VARCHAR(255)
                COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`authority`),
  UNIQUE KEY `authorities_authority_uindex` (`authority`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `user_authority`;
CREATE TABLE `user_authority` (
  `user`      VARCHAR(255)
              COLLATE utf8mb4_unicode_ci NOT NULL,
  `authority` VARCHAR(255)
              COLLATE utf8mb4_unicode_ci NOT NULL,
  KEY `user_authority_users_name_fk` (`user`),
  KEY `user_authority_authorities_name_fk` (`authority`),
  CONSTRAINT `user_authority_users_name_fk` FOREIGN KEY (`user`) REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `user_authority_authorities_name_fk` FOREIGN KEY (`authority`) REFERENCES `authorities` (`authority`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


LOCK TABLES `users` WRITE, `authorities` WRITE, `user_authority` WRITE;
INSERT INTO `users` VALUES
  ('admin', '{bcrypt}$2a$10$7FmXphF7JFK45uXwwmwTUeEVG6r9UedcJIoKAEYYKkjB5ZyQcFXeC', '', '', 1, 1, 1, 1),
  ('user', '{bcrypt}$2a$10$dVVaFsrQoUhskctl604rjOG3A2Rj5AMWYqNR3nF87DKgo3yTD3hDu', '', '', 1, 1, 1, 1),
  ('shop', '{bcrypt}$2a$10$zs9PnYWzL9GIlrIti.HrgOXZF329AviwNODwgTRIWQbasXZzEC49m', '', '', 1, 1, 1, 1);
INSERT INTO `authorities` VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard user'),
  ('FRONTEND', 'Site, forum, shop, etc.');
INSERT INTO `user_authority` VALUES
  ('admin', 'ADMIN'),
  ('admin', 'USER'),
  ('user', 'USER'),
  ('shop', 'FRONTEND');
UNLOCK TABLES;