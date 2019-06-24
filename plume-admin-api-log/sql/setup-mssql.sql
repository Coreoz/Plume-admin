CREATE TABLE PLM_LOG_API
(
   ID BIGINT NOT NULL,
   DATE DATETIME null,
   METHOD varchar(255) null,
   STATUS_CODE   varchar(255) null,
   BODY_REQUEST  CLOB   null,
   BODY_RESPONSE CLOB   null,
   API varchar(255) null,
   URL  varchar(255) null,
   CONSTRAINT plm_log_api_pk PRIMARY KEY (ID),
);


CREATE TABLE PLM_LOG_HEADER
(
 ID BIGINT NOT NULL,
 VALUE varchar(255) null,
 ID_LOG_API NUMBER (19,0) null,
 TYPE varchar(255) null,
 KEY varchar(255) null,
 CONSTRAINT plm_log_header_pk PRIMARY KEY (ID),
 CONSTRAINT plm_log_header_plm_log_api_id_fk FOREIGN KEY(ID_LOG_API) references PLM_LOG_API (ID)
);

INSERT INTO PLM_ROLE_PERMISSION VALUES(1, 'MANAGE_API_LOGS');

GO
