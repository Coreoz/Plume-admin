DROP TABLE IF EXISTS `PLM_USER_MFA`;
DROP TABLE IF EXISTS `PLM_ROLE_PERMISSION`;
DROP TABLE IF EXISTS `PLM_USER`;
DROP TABLE IF EXISTS `PLM_ROLE`;
CREATE TABLE  `PLM_ROLE` (
  `id` bigint(20) NOT NULL,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_plm_role_label` (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE  `PLM_USER` (
  `id` bigint(20) NOT NULL,
  `id_role` bigint(20) NOT NULL,
  `creation_date` datetime NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `mfa_user_handle` BLOB DEFAULT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_plm_user_email` (`email`),
  UNIQUE KEY `uniq_plm_user_username` (`user_name`),
  CONSTRAINT `plm_user_role` FOREIGN KEY (`id_role`) REFERENCES `PLM_ROLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE  `PLM_ROLE_PERMISSION` (
  `id_role` bigint(20) NOT NULL,
  `permission` varchar(255) NOT NULL,
  PRIMARY KEY (`id_role`, `permission`),
  CONSTRAINT `plm_role_permission_role` FOREIGN KEY (`id_role`) REFERENCES `PLM_ROLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `PLM_MFA`;
CREATE TABLE  `PLM_MFA` (
  `id` bigint(20) NOT NULL,
  `type` ENUM('authenticator', 'browser') NOT NULL,
  `secret_key` varchar(255) DEFAULT NULL,
  `credential_id` BLOB DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `PLM_USER_MFA` (
    `id_user` bigint(20) NOT NULL,
    `id_mfa` bigint(20) NOT NULL,
    PRIMARY KEY (`id_user`, `id_mfa`),
    CONSTRAINT `plm_user_mfa_user` FOREIGN KEY (`id_user`) REFERENCES `PLM_USER` (`id`),
    CONSTRAINT `plm_user_mfa_mfa` FOREIGN KEY (`id_mfa`) REFERENCES `PLM_MFA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO PLM_ROLE VALUES(1, 'Administrator');
INSERT INTO PLM_USER VALUES(1, 1, NOW(), 'Admin', 'Admin', 'admin@admin', 'admin', '$2a$11$FfgtfoHeNo/m9jGj9D5rTO0zDDI4LkMXnXHai744Ee32P3CHoBVqm', NULL);
INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_USERS');
INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_ROLES');
INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_SYSTEM');
