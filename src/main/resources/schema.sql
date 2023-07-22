DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS comments CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS items
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(510) NOT NULL,
    available BOOLEAN NOT NULL,
    owner BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    item_id BIGINT NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    booker BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(55) NOT NULL
);

CREATE TABLE IF NOT EXISTS comments
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text VARCHAR(510) NOT NULL,
    item_id BIGINT NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    author BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created TIMESTAMP NOT NULL
);