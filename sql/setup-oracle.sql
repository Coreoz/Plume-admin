

CREATE TABLE  plm_role (
  id NUMBER(19,0) NOT NULL,
  label varchar(255) NOT NULL,
  PRIMARY KEY  (id),
  CONSTRAINT uniq_plm_role_label UNIQUE (label)
);


CREATE TABLE  plm_user (
  id NUMBER (19,0) NOT NULL,
  id_role NUMBER (19,0) NOT NULL,
  creation_date DATE NOT NULL,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  user_name varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  PRIMARY KEY  (id),
  CONSTRAINT uniq_plm_user_email UNIQUE (email),
  CONSTRAINT uniq_plm_user_username UNIQUE (user_name),
  CONSTRAINT plm_user_role FOREIGN KEY (id_role) REFERENCES plm_role (id)
);


CREATE TABLE  plm_role_permission (
  id_role NUMBER (19,0) NOT NULL,
  permission varchar(255) NOT NULL,
  PRIMARY KEY (id_role, permission),
  CONSTRAINT plm_role_permission_role FOREIGN KEY (id_role) REFERENCES plm_role (id)
);


INSERT INTO plm_role VALUES(1, 'Administrator');
INSERT INTO plm_user VALUES(1, 1, SYSDATE, 'Admin', 'Admin', 'admin@admin', 'admin', '$2a$11$FfgtfoHeNo/m9jGj9D5rTO0zDDI4LkMXnXHai744Ee32P3CHoBVqm');
INSERT INTO plm_role_permission VALUES(1, 'MANAGE_USERS');
INSERT INTO plm_role_permission VALUES(1, 'MANAGE_ROLES');
INSERT INTO plm_role_permission VALUES(1, 'GENERIC_ACCESS');