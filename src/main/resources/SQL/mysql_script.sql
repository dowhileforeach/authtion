USE authtion;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE `users` (
  `id`                      BIGINT(20)                              NOT NULL   AUTO_INCREMENT,
  `email`                   VARCHAR(50)
                            COLLATE utf8mb4_unicode_ci              NOT NULL,
  `password`                VARCHAR(100)
                            COLLATE utf8mb4_unicode_ci              NOT NULL,
  `nick_name`               VARCHAR(20)
                            COLLATE utf8mb4_unicode_ci              NOT NULL   DEFAULT '',
  `first_name`              VARCHAR(20)
                            COLLATE utf8mb4_unicode_ci              NOT NULL   DEFAULT '',
  `last_name`               VARCHAR(20)
                            COLLATE utf8mb4_unicode_ci              NOT NULL   DEFAULT '',
  `account_non_expired`     TINYINT(1)                              NOT NULL   DEFAULT '1',
  `credentials_non_expired` TINYINT(1)                              NOT NULL   DEFAULT '1',
  `account_non_locked`      TINYINT(1)                              NOT NULL   DEFAULT '1',
  `enabled`                 TINYINT(1)                              NOT NULL   DEFAULT '1',
  `email_confirmed`         TINYINT(1)                              NOT NULL   DEFAULT '0',
  `created`                 DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  `updated`                 DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_id_uindex` (`id`),
  UNIQUE KEY `users_email_uindex` (`email`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT=1000
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
  `user`      BIGINT(20)                 NOT NULL,
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


DROP TABLE IF EXISTS `mailing_new_user_password`;
CREATE TABLE `mailing_new_user_password` (
  `user`     VARCHAR(50)
             COLLATE utf8mb4_unicode_ci             NOT NULL,
  `password` VARCHAR(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created`  DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated`  DATETIME                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user`),
  UNIQUE KEY `mailing_new_user_password_user_uindex` (`user`),
  CONSTRAINT `mailing_new_user_password_users_id_fk` FOREIGN KEY (`user`) REFERENCES `users` (`email`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `mailing_confirm_email`;
CREATE TABLE `mailing_confirm_email` (
  `user`         VARCHAR(50)
                 COLLATE utf8mb4_unicode_ci             NOT NULL,
  `confirm_key`  VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `already_sent` TINYINT(1)                             NOT NULL DEFAULT '0',
  `created`      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated`      DATETIME                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user`),
  UNIQUE KEY `mailing_confirm_email_user_uindex` (`user`),
  CONSTRAINT `mailing_confirm_email_users_id_fk` FOREIGN KEY (`user`) REFERENCES `users` (`email`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `mailing_restore_password`;
CREATE TABLE `mailing_restore_password` (
  `user`         VARCHAR(50)
                 COLLATE utf8mb4_unicode_ci             NOT NULL,
  `confirm_key`  VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `already_sent` TINYINT(1)                             NOT NULL DEFAULT '0',
  `created`      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated`      DATETIME                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user`),
  UNIQUE KEY `mailing_restore_password_user_uindex` (`user`),
  CONSTRAINT `mailing_restore_password_users_id_fk` FOREIGN KEY (`user`) REFERENCES `users` (`email`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


LOCK TABLES `users` WRITE, `authorities` WRITE, `user_authority` WRITE;
INSERT INTO `users` VALUES
  (1,   'admin@ya.ru', '{bcrypt}$2a$10$7FmXphF7JFK45uXwwmwTUeEVG6r9UedcJIoKAEYYKkjB5ZyQcFXeC', 'admin', '', '', 1, 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  (555, 'user@ya.ru',  '{bcrypt}$2a$10$dVVaFsrQoUhskctl604rjOG3A2Rj5AMWYqNR3nF87DKgo3yTD3hDu', 'user', '', '', 1, 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  (888, 'shop@ya.ru',  '{bcrypt}$2a$10$zs9PnYWzL9GIlrIti.HrgOXZF329AviwNODwgTRIWQbasXZzEC49m', 'shop', '', '', 1, 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07');
INSERT INTO `authorities` VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard user'),
  ('FRONTEND', 'Site, forum, etc.');
INSERT INTO `user_authority` VALUES
  (1, 'ADMIN'),
  (1, 'USER'),
  (555, 'USER'),
  (888, 'FRONTEND');
UNLOCK TABLES;