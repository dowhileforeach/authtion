USE authtion;

LOCK TABLES `consumers` WRITE, `authorities` WRITE, `consumer_authority` WRITE;
INSERT INTO `consumers` VALUES
  (1, 'admin@ya.ru', '{bcrypt}$2a$10$7FmXphF7JFK45uXwwmwTUeEVG6r9UedcJIoKAEYYKkjB5ZyQcFXeC', 'admin', '', '', 1, 1, 1,
      1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  (555, 'user@ya.ru', '{bcrypt}$2a$10$dVVaFsrQoUhskctl604rjOG3A2Rj5AMWYqNR3nF87DKgo3yTD3hDu', 'user', '', '', 1, 1, 1,
        1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07');
INSERT INTO `authorities` VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard consumer');
INSERT INTO `consumer_authority` VALUES
  (1, 'ADMIN'),
  (1, 'USER'),
  (555, 'USER');
UNLOCK TABLES;