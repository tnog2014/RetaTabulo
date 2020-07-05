CREATE TABLE IF NOT EXISTS login_user (
    user_id  serial   primary key,
    user_name VARCHAR(255),
    password VARCHAR(255),
    nickname VARCHAR(255),
    valid VARCHAR(1),
    level VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS board (
    id              serial   primary key,   -- id
    team_id         int,
    name            varchar(100),           -- name
    description     varchar(300)            -- description
);

CREATE TABLE IF NOT EXISTS description (
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

CREATE TABLE IF NOT EXISTS team (
    id              serial   primary key,   -- id
    name            varchar(100),           -- name
    description     varchar(300)            -- description
);


CREATE TABLE IF NOT EXISTS userteam (
    user_id        int,
    team_id        int,
    auth           varchar(1),
    PRIMARY KEY (user_id, team_id)
);

CREATE TABLE IF NOT EXISTS board_user (
    id  serial   primary key,
    user_id int,
    board_id int,
    sub_id VARCHAR(50),
    user_name VARCHAR(50),
    nickname VARCHAR(50),
    last_update_date timestamp
);


