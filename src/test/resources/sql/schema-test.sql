USE authtion_test;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS authtion_consumers;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE authtion_consumers (
  id                      BIGINT(20)                              NOT NULL   AUTO_INCREMENT,
  email                   VARCHAR(50)
                          COLLATE utf8mb4_unicode_ci              NOT NULL,
  password                VARCHAR(100)
                          COLLATE utf8mb4_unicode_ci              NOT NULL,
  nick_name               VARCHAR(20)
                          COLLATE utf8mb4_unicode_ci              NOT NULL   DEFAULT '',
  first_name              VARCHAR(20)
                          COLLATE utf8mb4_unicode_ci              NOT NULL   DEFAULT '',
  last_name               VARCHAR(20)
                          COLLATE utf8mb4_unicode_ci              NOT NULL   DEFAULT '',
  account_non_expired     TINYINT(1)                              NOT NULL   DEFAULT '1',
  credentials_non_expired TINYINT(1)                              NOT NULL   DEFAULT '1',
  account_non_locked      TINYINT(1)                              NOT NULL   DEFAULT '1',
  enabled                 TINYINT(1)                              NOT NULL   DEFAULT '1',
  email_confirmed         TINYINT(1)                              NOT NULL   DEFAULT '0',
  created_on              DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  updated_on              DATETIME                                           DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY authtion_consumers_id_uindex (id),
  UNIQUE KEY authtion_consumers_email_uindex (email)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS authtion_authorities;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE authtion_authorities (
  authority   VARCHAR(20)
              COLLATE utf8mb4_unicode_ci NOT NULL,
  description VARCHAR(100)
              COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (authority),
  UNIQUE KEY authtion_authorities_authority_uindex (authority)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS authtion_consumer_authority;
CREATE TABLE authtion_consumer_authority (
  consumer  BIGINT(20)                 NOT NULL,
  authority VARCHAR(20)
            COLLATE utf8mb4_unicode_ci NOT NULL,
  KEY authtion_consumer_authority_consumers_id_fk (consumer),
  KEY authtion_consumer_authority_authorities_authority_fk (authority),
  CONSTRAINT authtion_consumer_authority_consumers_id_fk FOREIGN KEY (consumer) REFERENCES authtion_consumers (id)
    ON DELETE CASCADE,
  CONSTRAINT authtion_consumer_authority_authorities_authority_fk FOREIGN KEY (authority) REFERENCES authtion_authorities (authority)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS authtion_mailing;
CREATE TABLE authtion_mailing (
  created_on            DATETIME                                 NOT NULL     DEFAULT CURRENT_TIMESTAMP,
  `type`                INT                                      NOT NULL,
  email                 VARCHAR(50)
                        COLLATE utf8mb4_unicode_ci               NOT NULL,
  sent                  TINYINT(1)                               NOT NULL,
  max_attempts_reached  TINYINT(1)                               NOT NULL,
  data                  VARCHAR(1000) COLLATE utf8mb4_unicode_ci NOT NULL     DEFAULT '',
  cause_of_last_failure VARCHAR(1000) COLLATE utf8mb4_unicode_ci NOT NULL     DEFAULT '',
  updated_on            DATETIME                                              DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (created_on, `type`, email),
  CONSTRAINT authtion_mailing_id_fk FOREIGN KEY (email) REFERENCES authtion_consumers (email)
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
CREATE TABLE IF NOT EXISTS oauth_access_token (
  token_id          VARCHAR(256),
  token             BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256),
  authentication    BLOB,
  refresh_token     VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_refresh_token (
  token_id       VARCHAR(256),
  token          BLOB,
  authentication BLOB
);