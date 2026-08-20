DO $$ BEGIN
    CREATE TYPE area_name AS ENUM ('UTTARA', 'GULSHAN', 'BANANI', 'DHANMONDI', 'BASHUNDHARA', 'MIRPUR', 'BANASREE', 'BARIDHARA');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE outage_type AS ENUM ('SCHEDULED', 'EMERGENCY');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE outage_status AS ENUM ('SCHEDULED', 'ONGOING', 'RESOLVED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS outages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    area area_name NOT NULL,
    type outage_type NOT NULL,
    status outage_status NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    estimated_end_time TIMESTAMP WITH TIME ZONE,
    actual_end_time TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outages_area ON outages(area);
CREATE INDEX IF NOT EXISTS idx_outages_status ON outages(status);
CREATE INDEX IF NOT EXISTS idx_outages_start_time ON outages(start_time DESC);
