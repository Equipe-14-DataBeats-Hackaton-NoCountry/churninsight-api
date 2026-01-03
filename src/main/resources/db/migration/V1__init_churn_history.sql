CREATE TABLE churn_history(
    id CHAR(36) NOT NULL PRIMARY KEY,

    -- DADOS DE ENTRADA
    gender VARCHAR(20),
    age INT,
    country VARCHAR(10),
    subscription_type VARCHAR(30),
    listening_time DOUBLE,
    songs_played_per_day INT,
    skip_rate DOUBLE,
    ads_listened_per_week INT,
    device_type VARCHAR(30),
    offline_listening BOOLEAN,
    user_id CHAR(36),

    -- SAÍDA DO MODELO
    churn_status ENUM('WILL_CHURN', 'WILL_STAY') NOT NULL,
    probability DOUBLE,

    -- AUDITORIA
    requester_id CHAR(36),
    request_ip VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);