CREATE TABLE sleep_log (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    sleep_date      DATE            NOT NULL,
    bed_time        TIMESTAMP       NOT NULL,
    wake_time       TIMESTAMP       NOT NULL,
    morning_feeling VARCHAR(4)      NOT NULL CHECK (morning_feeling IN ('BAD', 'OK', 'GOOD')),
    created_at      TIMESTAMP       DEFAULT NOW(),

    CONSTRAINT uq_user_sleep_date UNIQUE (user_id, sleep_date)
);
