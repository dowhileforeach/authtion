USE authtion_dev;

# Password for consumer test1@dwfe.ru = test11
# Password for consumer test2@dwfe.ru = test22
LOCK TABLES authtion_consumers WRITE, authtion_authorities WRITE, authtion_consumer_authority WRITE;
INSERT INTO authtion_consumers VALUES
  (1000, 'test1@dwfe.ru', '{bcrypt}$2a$10$cWUX5MiFl8rJFVxKxEbON.2QcJ/0RsVfhVvvqDG5wEOM/bstMIk6m', 'test1', '', '', 1, 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  (1001, 'test2@dwfe.ru', '{bcrypt}$2a$10$9SCLBifjy2Ieaoc6VLmSgOQsxf4NUlbGO32zMraftTXcl3jEAqlbm', 'test2', '', '', 1, 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07');
INSERT INTO authtion_authorities VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard consumer');
INSERT INTO authtion_consumer_authority VALUES
  (1000, 'ADMIN'),
  (1000, 'USER'),
  (1001, 'USER');
UNLOCK TABLES;