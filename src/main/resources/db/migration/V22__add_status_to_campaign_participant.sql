-- Bước 1: Thêm cột status (cho phép NULL tạm thời để tránh lỗi với dữ liệu cũ)
ALTER TABLE campaign_participant 
ADD COLUMN status VARCHAR(255);

-- Bước 2: Data Migration 
-- Cập nhật tất cả nhân viên đang có trong bảng thành 'JOINED'
-- Vì logic cũ chưa có tính năng rời, nên ai đang ở trong bảng này nghĩa là họ ĐANG THAM GIA.
UPDATE campaign_participant 
SET status = 'JOINED' 
WHERE status IS NULL;

-- Bước 3: Enforce Constraint (Bắt buộc có dữ liệu)
-- Sau khi đã lấp đầy dữ liệu ở Bước 2, ta set cột này thành NOT NULL để đảm bảo tính toàn vẹn
ALTER TABLE campaign_participant ALTER COLUMN status SET NOT NULL;