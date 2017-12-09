USE authtion;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE `users` (
  `id`                      VARCHAR(100)
                            COLLATE utf8mb4_unicode_ci              NOT NULL,
  `password`                VARCHAR(100)
                            COLLATE utf8mb4_unicode_ci              NOT NULL,
  `first_name`              VARCHAR(20)
                            COLLATE utf8mb4_unicode_ci                       DEFAULT '',
  `last_name`               VARCHAR(20)
                            COLLATE utf8mb4_unicode_ci                       DEFAULT '',
  `account_non_expired`     TINYINT(1)                              NOT NULL DEFAULT '1',
  `credentials_non_expired` TINYINT(1)                              NOT NULL DEFAULT '1',
  `account_non_locked`      TINYINT(1)                              NOT NULL DEFAULT '1',
  `enabled`                 TINYINT(1)                              NOT NULL DEFAULT '1',
  `created`                 DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  `updated`                 DATETIME                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_id_uindex` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `authorities`;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE `authorities` (
  `authority`   VARCHAR(20)
                COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` VARCHAR(100)
                COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`authority`),
  UNIQUE KEY `authorities_authority_uindex` (`authority`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `user_authority`;
CREATE TABLE `user_authority` (
  `user`      VARCHAR(100)
              COLLATE utf8mb4_unicode_ci NOT NULL,
  `authority` VARCHAR(20)
              COLLATE utf8mb4_unicode_ci NOT NULL,
  KEY `user_authority_users_id_fk` (`user`),
  KEY `user_authority_authorities_authority_fk` (`authority`),
  CONSTRAINT `user_authority_users_id_fk` FOREIGN KEY (`user`) REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `user_authority_authorities_authority_fk` FOREIGN KEY (`authority`) REFERENCES `authorities` (`authority`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `confirmation_key`;
CREATE TABLE `confirmation_key` (
  `user`              VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `confirm_key`       VARCHAR(50) COLLATE utf8mb4_unicode_ci  NOT NULL,
  `created`           DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  `create_new_user`   TINYINT(1)                              NOT NULL,
  `restore_user_pass` TINYINT(1)                              NOT NULL,
  PRIMARY KEY (`user`),
  UNIQUE KEY `confirmation_key_user_uindex` (`user`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


LOCK TABLES `users` WRITE, `authorities` WRITE, `user_authority` WRITE;
INSERT INTO `users` VALUES
  ('admin@ya.ru', '{bcrypt}$2a$10$7FmXphF7JFK45uXwwmwTUeEVG6r9UedcJIoKAEYYKkjB5ZyQcFXeC', '', '', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  ('user@ya.ru', '{bcrypt}$2a$10$dVVaFsrQoUhskctl604rjOG3A2Rj5AMWYqNR3nF87DKgo3yTD3hDu', '', '', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  ('shop@ya.ru', '{bcrypt}$2a$10$zs9PnYWzL9GIlrIti.HrgOXZF329AviwNODwgTRIWQbasXZzEC49m', '', '', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07');
INSERT INTO `authorities` VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard user'),
  ('FRONTEND', 'Site, forum, etc.');
INSERT INTO `user_authority` VALUES
  ('admin@ya.ru', 'ADMIN'),
  ('admin@ya.ru', 'USER'),
  ('user@ya.ru', 'USER'),
  ('shop@ya.ru', 'FRONTEND');
UNLOCK TABLES;