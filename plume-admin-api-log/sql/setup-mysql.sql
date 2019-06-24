create table plm_log_api
(
  id            bigint       not null
    primary key,
  date          datetime    not null,
  method        varchar(255) not null,
  status_code   varchar(255) not null,
  body_request  mediumtext   null,
  body_response mediumtext   null,
  api           varchar(255) not null,
  url           varchar(255) not null
);


create table plm_log_header
(
  id         bigint       not null
    primary key,
  value      varchar(255) not null,
  id_log_api bigint       not null,
  type       varchar(255) not null,
  `key`      varchar(255) not null,
  constraint plm_log_header_plm_log_api_id_fk
    foreign key (id_log_api) references plm_log_api (id)
);

INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_API_LOGS');
