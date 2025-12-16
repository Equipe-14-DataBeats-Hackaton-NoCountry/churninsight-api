CREATE TABLE churn_history (
    id CHAR(36) NOT NULL PRIMARY KEY,


        -- DADOS DE ENTRADA

    gender VARCHAR(20),
    age INT,
    country VARCHAR(10),
    subscription_type VARCHAR(30),
    listening_time DOUBLE,
    songs_played INT,
    skip_rate DOUBLE,
    device_type VARCHAR(30),
    offline_listening BOOLEAN,
    user_id CHAR(36),


        -- SAÍDA DO MODELO

    will_churn BOOLEAN,
    probability DOUBLE,
    churn_label VARCHAR(30),

        -- AUDITORIA

    requester_id CHAR(36),
    request_ip VARCHAR(45),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

