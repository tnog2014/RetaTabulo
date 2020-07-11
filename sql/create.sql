create table login_user (
    user_id  serial   primary key,
    user_name VARCHAR(255),
    password VARCHAR(255),
    nickname VARCHAR(255),
    valid VARCHAR(1),
    level VARCHAR(1)
);

create table board (
    id              serial   primary key,   -- id
    team_id         int,
    name            varchar(100),           -- name
    description     varchar(300)            -- description
);

create table description (
    id              serial   primary key,   -- id
    board_id        int,                     -- board_Id
    create_user     int,                     -- create_user
    update_user     int,                     -- update_user
    x               real,                    -- x
    y               real,                    -- y
    width           real,                    -- width
    height          real,                    -- height
    raw             text,                    -- raw
    html            text                     -- html
);

create table team (
    id              serial   primary key,   -- id
    name            varchar(100),           -- name
    description     varchar(300)            -- description
);


create table userteam (
    user_id        int,
    team_id        int,
    auth           varchar(1),
    PRIMARY KEY (user_id, team_id)
);

create table board_user (
    id  serial   primary key,
    user_id int,
    board_id int,
    sub_id VARCHAR(50),
    user_name VARCHAR(50),
    nickname VARCHAR(50),
    last_update_date timestamp
);

CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BYTEA NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);


