-- Add new fields to campaign table
ALTER TABLE campaign 
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'draft',
ADD COLUMN image_url VARCHAR(500),
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Update existing campaigns to have proper status based on dates
UPDATE campaign 
SET status = CASE 
    WHEN end_date < CURRENT_DATE THEN 'completed'
    WHEN start_date <= CURRENT_DATE AND end_date >= CURRENT_DATE THEN 'active'
    ELSE 'draft'
END;

-- Add constraints
ALTER TABLE campaign 
ADD CONSTRAINT chk_campaign_status CHECK (status IN ('draft', 'active', 'completed'));

ALTER TABLE campaign 
ADD CONSTRAINT chk_campaign_dates CHECK (end_date >= start_date);

-- Create indexes for better performance
CREATE INDEX idx_campaign_status ON campaign(status);
CREATE INDEX idx_campaign_dates ON campaign(start_date, end_date);
CREATE INDEX idx_campaign_created_at ON campaign(created_at DESC);