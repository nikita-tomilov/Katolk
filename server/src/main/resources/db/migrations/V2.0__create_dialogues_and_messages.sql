CREATE TABLE dialogue
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    creator_id  BIGINT NOT NULL,
    name        TEXT NOT NULL
);

CREATE TABLE dialogue_participant
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    dialogue_id BIGINT NOT NULL,
    user_id     BIGINT NOT NULL
);

CREATE TABLE message
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    author      BIGINT NOT NULL,
    dialogue_id BIGINT NOT NULL,
    body        TEXT NOT NULL,
    timestamp   BIGINT NOT NULL,
    was_read    BOOLEAN NOT NULL
);