-- Thêm cột current_score vào bảng campaign_participant
-- Mặc định là 0 để các dữ liệu cũ không bị null
ALTER TABLE campaign_participant 
ADD COLUMN current_score DOUBLE PRECISION DEFAULT 0.0;