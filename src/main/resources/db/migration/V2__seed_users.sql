INSERT INTO users (name, surname, email, created_at, updated_at)
VALUES
    ('Ivan', 'Petrov', 'ivan.petrov@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Anna', 'Smirnova', 'anna.smirnova@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Maksim', 'Sokolov', 'maksim.sokolov@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
