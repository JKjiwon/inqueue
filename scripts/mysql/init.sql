-- member 테이블
CREATE TABLE member
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date_time  DATETIME(6),
    modified_date_time DATETIME(6),
    client_id          VARCHAR(255),
    client_secret      VARCHAR(255),
    name               VARCHAR(255),
    phone              VARCHAR(255)
);

-- member_role 테이블
CREATE TABLE member_role
(
    member_id BIGINT NOT NULL,
    role      VARCHAR(255),
    CONSTRAINT fk_member_role_member FOREIGN KEY (member_id) REFERENCES member (id)
);

-- event 테이블
CREATE TABLE event
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date_time    DATETIME(6),
    modified_date_time   DATETIME(6),
    event_id             VARCHAR(255) NOT NULL,
    job_queue_limit_time BIGINT       NOT NULL,
    job_queue_size       BIGINT       NOT NULL,
    end_date_time        DATETIME(6) NOT NULL,
    start_date_time      DATETIME(6) NOT NULL,
    redirect_url         VARCHAR(255),
    member_id            BIGINT,
    CONSTRAINT uk_event UNIQUE (event_id),
    CONSTRAINT fk_event_member FOREIGN KEY (member_id) REFERENCES member (id)
);

-- 인덱스 생성
CREATE INDEX idx_event_id ON event (event_id);
