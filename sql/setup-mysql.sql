DROP TABLE IF EXISTS `plm_role`;
CREATE TABLE  `plm_role` (
  `id` bigint(20) NOT NULL,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_plm_role_label` (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `plm_user`;
CREATE TABLE  `plm_user` (
  `id` bigint(20) NOT NULL,
  `id_role` bigint(20) NOT NULL,
  `creation_date` datetime NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_plm_user_email` (`email`),
  UNIQUE KEY `uniq_plm_user_username` (`user_name`),
  CONSTRAINT `plm_user_role` FOREIGN KEY (`id_role`) REFERENCES `plm_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `plm_role_permission`;
CREATE TABLE  `plm_role_permission` (
  `id_role` bigint(20) NOT NULL,
  `permission` varchar(255) NOT NULL,
  PRIMARY KEY (`id_role`, `permission`),
  CONSTRAINT `plm_role_permission_role` FOREIGN KEY (`id_role`) REFERENCES `plm_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO plm_role VALUES(1, 'Administrator');
INSERT INTO plm_user VALUES(1, 1, NOW(), 'Admin', 'Admin', 'admin@admin', 'admin', '$2a$11$FfgtfoHeNo/m9jGj9D5rTO0zDDI4LkMXnXHai744Ee32P3CHoBVqm');
INSERT INTO plm_role_permission VALUES(1, 'MANAGE_USERS');
INSERT INTO plm_role_permission VALUES(1, 'SEE_ROLES');
INSERT INTO plm_role_permission VALUES(1, 'MANAGE_ROLES');