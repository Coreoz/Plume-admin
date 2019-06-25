DROP TABLE IF EXISTS `PLM_ROLE`;
CREATE TABLE  `PLM_ROLE` (
  `id` bigint(20) NOT NULL,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_plm_role_label` (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `PLM_USER`;
CREATE TABLE  `PLM_USER` (
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
  CONSTRAINT `plm_user_role` FOREIGN KEY (`id_role`) REFERENCES `PLM_ROLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `PLM_ROLE_PERMISSION`;
CREATE TABLE  `PLM_ROLE_PERMISSION` (
  `id_role` bigint(20) NOT NULL,
  `permission` varchar(255) NOT NULL,
  PRIMARY KEY (`id_role`, `permission`),
  CONSTRAINT `plm_role_permission_role` FOREIGN KEY (`id_role`) REFERENCES `PLM_ROLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO PLM_ROLE VALUES(1, 'Administrator');
INSERT INTO PLM_USER VALUES(1, 1, NOW(), 'Admin', 'Admin', 'admin@admin', 'admin', '$2a$11$FfgtfoHeNo/m9jGj9D5rTO0zDDI4LkMXnXHai744Ee32P3CHoBVqm');
INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_USERS');
INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_ROLES');
INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_SYSTEM');
