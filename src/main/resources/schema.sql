USE authtion;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `consumers`;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE `consumers` (
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
  `created_on`              DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  `updated_on`              DATETIME                                           DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `consumers_id_uindex` (`id`),
  UNIQUE KEY `consumers_email_uindex` (`email`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1000
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


DROP TABLE IF EXISTS `consumer_authority`;
CREATE TABLE `consumer_authority` (
  `consumer`  BIGINT(20)                 NOT NULL,
  `authority` VARCHAR(20)
              COLLATE utf8mb4_unicode_ci NOT NULL,
  KEY `consumer_authority_consumers_id_fk` (`consumer`),
  KEY `consumer_authority_authorities_authority_fk` (`authority`),
  CONSTRAINT `consumer_authority_consumers_id_fk` FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `consumer_authority_authorities_authority_fk` FOREIGN KEY (`authority`) REFERENCES `authorities` (`authority`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `mailing_welcome_when_password_was_not_passed`;
CREATE TABLE `mailing_welcome_when_password_was_not_passed` (
  `created_on`           DATETIME                               NOT NULL   DEFAULT CURRENT_TIMESTAMP,
  `email`                VARCHAR(50)
                         COLLATE utf8mb4_unicode_ci             NOT NULL,
  `password`             VARCHAR(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sended`               TINYINT(1)                             NOT NULL   DEFAULT '0',
  `max_attempts_reached` TINYINT(1)                             NOT NULL   DEFAULT '0',
  `updated_on`           DATETIME                                          DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`email`),
  UNIQUE KEY `mailing_welcome_when_password_was_not_passed_consumer_uindex` (`email`),
  CONSTRAINT `mailing_welcome_when_password_was_not_passed_consumers_id_fk` FOREIGN KEY (`email`) REFERENCES `consumers` (`email`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `mailing_confirm_email`;
CREATE TABLE `mailing_confirm_email` (
  `consumer`     VARCHAR(50)
                 COLLATE utf8mb4_unicode_ci             NOT NULL,
  `confirm_key`  VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `already_sent` TINYINT(1)                             NOT NULL DEFAULT '0',
  `created_on`   DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_on`   DATETIME                                        DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`consumer`),
  UNIQUE KEY `mailing_confirm_email_consumer_uindex` (`consumer`),
  CONSTRAINT `mailing_confirm_email_consumers_id_fk` FOREIGN KEY (`consumer`) REFERENCES `consumers` (`email`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS `mailing_restore_password`;
CREATE TABLE `mailing_restore_password` (
  `consumer`     VARCHAR(50)
                 COLLATE utf8mb4_unicode_ci             NOT NULL,
  `confirm_key`  VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `already_sent` TINYINT(1)                             NOT NULL DEFAULT '0',
  `created_on`   DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_on`   DATETIME                                        DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`consumer`),
  UNIQUE KEY `mailing_restore_password_consumer_uindex` (`consumer`),
  CONSTRAINT `mailing_restore_password_consumers_id_fk` FOREIGN KEY (`consumer`) REFERENCES `consumers` (`email`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

#
# To persist tokens between server restarts I need:
# 1) to configure a persistent token store (JdbcTokenStore for example, see config/TokenStoreConfig.java)
# 2) create SQL tables: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql
#    (minimum):
# CREATE TABLE oauth_access_token (
#   token_id VARCHAR(256),
#   token BLOB,
#   authentication_id VARCHAR(256) PRIMARY KEY,
#   user_name VARCHAR(256),
#   client_id VARCHAR(256),
#   authentication BLOB,
#   refresh_token VARCHAR(256)
# );
#
# CREATE TABLE oauth_refresh_token (
#   token_id VARCHAR(256),
#   token BLOB,
#   authentication BLOB
# );