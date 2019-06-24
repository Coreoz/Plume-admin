create table plm_log_api
(
  id            bigint       not null
    primary key,
  date          timestamp    null,
  method        varchar(255) null,
  status_code   varchar(255) null,
  body_request  mediumtext   null,
  body_response mediumtext   null,
  api           varchar(255) null,
  url           varchar(255) null
);


create table plm_log_header
(
  id         bigint       not null
    primary key,
  value      varchar(255) null,
  id_log_api bigint       null,
  type       varchar(255) null,
  `key`      varchar(255) null,
  constraint plm_log_header_plm_log_api_id_fk
    foreign key (id_log_api) references plm_log_api (id)
);

INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_API_LOGS');
