DROP TABLE IF EXISTS `pl_bo_role`;
CREATE TABLE  `pl_bo_role` (
  `id` bigint(20) NOT NULL,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_pl_bo_role_label` (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pl_bo_user`;
CREATE TABLE  `pl_bo_user` (
  `id` bigint(20) NOT NULL,
  `id_role` bigint(20) NOT NULL,
  `creation_date` datetime NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_pl_bo_user_email` (`email`),
  UNIQUE KEY `uniq_pl_bo_user_username` (`user_name`),
  CONSTRAINT `pl_bo_user_role` FOREIGN KEY (`id_role`) REFERENCES `pl_bo_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pl_bo_role_permission`;
CREATE TABLE  `pl_bo_role_permission` (
  `id` bigint(20) NOT NULL,
  `id_role` bigint(20) NOT NULL,
  `permission` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uniq_pl_bo_role_permission` (`id_role`, `permission`),
  CONSTRAINT `pl_bo_role_permission_role` FOREIGN KEY (`id_role`) REFERENCES `pl_bo_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;