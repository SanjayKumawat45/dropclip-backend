CREATE TABLE drop_prompts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    prompt_text VARCHAR(300) NOT NULL,
    title VARCHAR(200) NOT NULL,
    is_used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO drop_prompts (title, prompt_text) VALUES
('Hidden Talent', 'What is your hidden talent? Show us in 60 seconds!'),
('Morning Routine', 'Show us the first 60 seconds of your morning routine'),
('Best Dance Move', 'Teach us your best dance move right now!'),
('Unpopular Opinion', 'Share your most unpopular opinion about anything'),
('Life Hack', 'Show us a life hack that actually works'),
('Funniest Moment', 'Recreate the funniest thing that happened to you this week'),
('Secret Skill', 'Show us a skill nobody knows you have'),
('Food Review', 'Review the last thing you ate in 60 seconds'),
('Plot Twist', 'Tell us a story with the most unexpected plot twist'),
('Challenge Accepted', 'Do the most impressive thing you can do right now');