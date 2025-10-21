-- Tee Time Monitoring System Database Schema
-- All statements use IF NOT EXISTS for idempotency
-- Uses UUID v7 for primary keys

-- Table: search_criteria
-- Stores reusable search criteria that can be shared across multiple users
CREATE TABLE IF NOT EXISTS search_criteria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    radius_miles INTEGER NOT NULL,
    search_date VARCHAR(20) NOT NULL,  -- Format: "Oct 11 2025"
    number_of_players INTEGER NOT NULL,
    preferred_time_start INTEGER NOT NULL DEFAULT 5,  -- e.g., 10 = 10:00 AM
    preferred_time_end INTEGER NOT NULL DEFAULT 21,   -- e.g., 18 = 6:00 PM
    max_price INTEGER,                 -- in dollars
    hot_deals_only BOOLEAN NOT NULL DEFAULT false,
    holes INTEGER NOT NULL,            -- 1=9 holes, 2=18 holes, 3=both
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Unique constraint to prevent duplicate criteria
    CONSTRAINT unique_search_criteria UNIQUE (
        latitude, longitude, radius_miles, search_date,
        number_of_players, preferred_time_start, preferred_time_end,
        hot_deals_only, holes
    )
);

-- Index for efficient lookups
CREATE INDEX IF NOT EXISTS idx_search_criteria_lookup ON search_criteria (
    latitude, longitude, search_date
);

-- Table: priority_courses
-- Stores the priority course IDs for each search criteria (one-to-many relationship)
CREATE TABLE IF NOT EXISTS priority_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    search_criteria_id UUID NOT NULL REFERENCES search_criteria(id) ON DELETE CASCADE,
    facility_id INTEGER NOT NULL,

    CONSTRAINT unique_priority_course UNIQUE (search_criteria_id, facility_id)
);

CREATE INDEX IF NOT EXISTS idx_priority_courses_criteria ON priority_courses(search_criteria_id);

-- Table: user_search_preferences
-- Links users to their search criteria with schedule settings
CREATE TABLE IF NOT EXISTS user_search_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    search_criteria_id UUID NOT NULL REFERENCES search_criteria(id) ON DELETE CASCADE,
    payment_enabled BOOLEAN NOT NULL DEFAULT false,
    notify_enabled BOOLEAN NOT NULL DEFAULT true,
    schedule_id VARCHAR(255),          -- Temporal schedule ID
    schedule_interval VARCHAR(50),     -- ISO 8601 duration (e.g., "PT5M")
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT unique_user_search UNIQUE (email, search_criteria_id)
);

CREATE INDEX IF NOT EXISTS idx_user_search_email ON user_search_preferences(email);
CREATE INDEX IF NOT EXISTS idx_user_search_schedule ON user_search_preferences(schedule_id);
CREATE INDEX IF NOT EXISTS idx_user_search_active ON user_search_preferences(active) WHERE active = true;

-- Table: tee_time_results
-- Tracks individual tee time slots discovered across all searches
-- Shared globally across all users and searches - deduplicated by facility + tee time
CREATE TABLE IF NOT EXISTS tee_time_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facility_id INTEGER NOT NULL,
    facility_name VARCHAR(255) NOT NULL,
    tee_time TIMESTAMP NOT NULL,       -- The actual tee time slot
    price DECIMAL(10,2) NOT NULL,
    booking_url TEXT NOT NULL,
    first_seen_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT unique_tee_time UNIQUE (facility_id, tee_time)
);

CREATE INDEX IF NOT EXISTS idx_tee_time_facility ON tee_time_results(facility_id);
CREATE INDEX IF NOT EXISTS idx_tee_time_slot ON tee_time_results(tee_time);
CREATE INDEX IF NOT EXISTS idx_tee_time_last_seen ON tee_time_results(last_seen_at);

-- Table: user_notifications
-- Tracks which tee times have been notified to which users
CREATE TABLE IF NOT EXISTS user_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_search_preference_id UUID NOT NULL REFERENCES user_search_preferences(id) ON DELETE CASCADE,
    tee_time_result_id UUID NOT NULL REFERENCES tee_time_results(id) ON DELETE CASCADE,
    notified_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT unique_user_notification UNIQUE (user_search_preference_id, tee_time_result_id)
);

CREATE INDEX IF NOT EXISTS idx_user_notif_preference ON user_notifications(user_search_preference_id);
CREATE INDEX IF NOT EXISTS idx_user_notif_result ON user_notifications(tee_time_result_id);

CREATE TABLE IF NOT EXISTS email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    html_body TEXT,
    text_body TEXT,
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
