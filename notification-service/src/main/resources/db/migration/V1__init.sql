DO $$ BEGIN
    CREATE TYPE area_name AS ENUM ('UTTARA', 'GULSHAN', 'BANANI', 'DHANMONDI', 'BASHUNDHARA', 'MIRPUR', 'BANASREE', 'BARIDHARA');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE notification_type AS ENUM ('OUTAGE', 'COMPLAINT_UPDATE', 'PAYMENT', 'ANNOUNCEMENT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    area area_name,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    related_entity_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_area ON notifications(area);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
