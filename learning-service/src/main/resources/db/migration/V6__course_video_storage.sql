-- Course videos are stored in object storage (Amazon S3 on AWS, SeaweedFS locally).
-- The database keeps only metadata; youtube_url stays as a fallback for courses without an upload.
ALTER TABLE courses ADD COLUMN video_key VARCHAR(300);
ALTER TABLE courses ADD COLUMN video_content_type VARCHAR(100);
ALTER TABLE courses ADD COLUMN video_size_bytes BIGINT;
ALTER TABLE courses ADD COLUMN video_uploaded_at TIMESTAMPTZ;
