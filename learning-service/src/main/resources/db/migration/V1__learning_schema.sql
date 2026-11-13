CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS course_units (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    unit_number INTEGER NOT NULL,
    title VARCHAR(180) NOT NULL,
    outline TEXT NOT NULL,
    CONSTRAINT uq_course_unit_number UNIQUE (course_id, unit_number)
);

CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    unit_id BIGINT NOT NULL REFERENCES course_units(id),
    title VARCHAR(180) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    explanation TEXT NOT NULL,
    question_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS quiz_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    correct BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
    score INTEGER NOT NULL,
    total INTEGER NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS study_reminders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    unit_id BIGINT NOT NULL REFERENCES course_units(id),
    due_at TIMESTAMPTZ NOT NULL,
    notified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_course_units_order ON course_units(course_id, unit_number);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_expiry ON refresh_tokens(user_id, expires_at);
CREATE INDEX IF NOT EXISTS idx_quizzes_unit_created ON quizzes(unit_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_attempts_user_quiz_time ON quiz_attempts(user_id, quiz_id, completed_at DESC) INCLUDE (score, total);
CREATE INDEX IF NOT EXISTS idx_reminders_due ON study_reminders(due_at) WHERE notified_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_reminders_user_due ON study_reminders(user_id, due_at DESC);
