DROP TABLE IF EXISTS "app_user" CASCADE;
DROP TABLE IF EXISTS "activity" CASCADE;
DROP TABLE IF EXISTS "challenge" CASCADE;
DROP TABLE IF EXISTS "challenge_participants" CASCADE;

CREATE TABLE "app_user" (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    points INTEGER DEFAULT 0
);

CREATE TABLE "activity" (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    details TEXT,
    emissions_kg DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "app_user"(id)
);

CREATE TABLE "challenge" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    goal DOUBLE PRECISION NOT NULL
);

CREATE TABLE "challenge_participants" (
    challenge_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (challenge_id, user_id),
    FOREIGN KEY (challenge_id) REFERENCES "challenge"(id),
    FOREIGN KEY (user_id) REFERENCES "app_user"(id)
);
