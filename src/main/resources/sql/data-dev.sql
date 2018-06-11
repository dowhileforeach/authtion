USE authtion_dev;

LOCK TABLES
authtion_consumers WRITE,
authtion_countries WRITE,
authtion_genders WRITE,
authtion_users WRITE,
authtion_authorities WRITE,
authtion_consumer_authority WRITE;

-- Password for consumer test1@dwfe.ru = test11
-- Password for consumer test2@dwfe.ru = test22
INSERT INTO authtion_consumers VALUES
  (1000, 'test1@dwfe.ru', 1, 1, '{bcrypt}$2a$10$cWUX5MiFl8rJFVxKxEbON.2QcJ/0RsVfhVvvqDG5wEOM/bstMIk6m', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  (1001, 'test2@dwfe.ru', 1, 1, '{bcrypt}$2a$10$9SCLBifjy2Ieaoc6VLmSgOQsxf4NUlbGO32zMraftTXcl3jEAqlbm', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07');
INSERT INTO authtion_countries VALUES
  ('Russia', 'RU', 'RUS', '7'),
  ('Ukraine', 'UA', 'UKR', '380'),
  ('Germany', 'DE', 'DEU', '49'),
  ('United States', 'US', 'USA', '1'),
  ('United Kingdom', 'GB', 'GBR', '44'),
  ('Japan', 'JP', 'JPN', '81');
INSERT INTO authtion_genders VALUES
  ('M', 'Male'),
  ('F', 'Female');
INSERT INTO authtion_users VALUES
  (1000, 'test1', 1, null, 1, null, 1, null, 1, null, 1, null, 1, 'RU', 1, 'Moscow', 1, 'pzn', 1, '2017-07-07 07:07:07'),
  (1001, 'test2', 1, null, 1, null, 1, null, 1, null, 1, null, 1, 'JP', 1, 'Tokyo', 1, null, 1, '2017-07-07 07:07:07');
INSERT INTO authtion_authorities VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard consumer');
INSERT INTO authtion_consumer_authority VALUES
  (1000, 'ADMIN'),
  (1000, 'USER'),
  (1001, 'USER');

UNLOCK TABLES;