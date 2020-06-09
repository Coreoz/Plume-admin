create table plm_log_api
(
  id            number(19)       not null primary key,
  date          timestamp    not null,
  method        varchar2(255) not null,
  status_code   NUMBER(5) not null,
  body_request  clob   null,
  body_response clob   null,
  apiName           varchar2(255) not null,
  url           CLOB not null
);

create table plm_log_header
(
  id         number(19)       not null primary key,
  id_log_api number(19)       not null,
  type       varchar2(255) not null,
  name      varchar2(255) not null,
  value      CLOB not null,
  constraint plm_log_header_plm_log_api_id_fk
    foreign key (id_log_api) references plm_log_api (id)
);

INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_API_LOGS');
