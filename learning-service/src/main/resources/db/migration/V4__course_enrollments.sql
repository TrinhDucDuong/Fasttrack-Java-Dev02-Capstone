CREATE TABLE course_enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_course_enrollment UNIQUE (user_id, course_id)
);

CREATE INDEX idx_course_enrollments_user ON course_enrollments(user_id, created_at DESC);
