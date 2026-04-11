-- Users table
CREATE TABLE users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio VARCHAR(300),
    streak_count INTEGER DEFAULT 0,
    total_clips INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Drops table (daily challenges)
CREATE TABLE drops (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    prompt_text VARCHAR(300) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clips table (video responses to drops)
CREATE TABLE clips (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    drop_id UUID NOT NULL REFERENCES drops(id),
    title VARCHAR(200),
    s3_key VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PROCESSING',
    vote_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, drop_id)
);

-- Votes table
CREATE TABLE votes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    clip_id UUID NOT NULL REFERENCES clips(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, clip_id)
);

-- Badges table
CREATE TABLE badges (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    badge_type VARCHAR(50) NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_clips_drop_id ON clips(drop_id);
CREATE INDEX idx_clips_user_id ON clips(user_id);
CREATE INDEX idx_votes_clip_id ON votes(clip_id);
CREATE INDEX idx_drops_is_active ON drops(is_active);