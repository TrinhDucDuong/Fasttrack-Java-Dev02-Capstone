ALTER TABLE courses ADD COLUMN price NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN youtube_url VARCHAR(500);
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';
CREATE INDEX idx_courses_title ON courses USING GIN (to_tsvector('english', title));
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_courses_title_substring ON courses USING GIN (upper(title) gin_trgm_ops);
UPDATE courses SET price=29.99, youtube_url='https://www.youtube.com/watch?v=GoXwIVyNvX0' WHERE slug='middle-java';
