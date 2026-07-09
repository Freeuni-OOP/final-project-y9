CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    birth_date DATE NOT NULL,
    isBanned BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    );


CREATE TABLE IF NOT EXISTS friendships (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           sender_id BIGINT NOT NULL,
                                           receiver_id BIGINT NOT NULL,
                                           status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS messages (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        sender_id BIGINT NOT NULL,
                                        room_id BIGINT NOT NULL,
                                        message TEXT NOT NULL,
                                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES chatrooms(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS chatrooms (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(100) DEFAULT NULL,               -- e.g., "Joker Lobby #432" or NULL for private 1-on-1s
    type VARCHAR(50) NOT NULL,                    -- 'PRIVATE', 'GROUP', or 'GAME_LOBBY'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS chatroom_members (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                room_id BIGINT NOT NULL,
                                                user_id BIGINT NOT NULL,
                                                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (room_id) REFERENCES chatrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_room_user (room_id, user_id) -- Ensures a user can't join the same room twice
    );

CREATE TABLE IF NOT EXISTS achievements (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS user_achievements (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 user_id BIGINT NOT NULL,
                                                 achievement_id BIGINT NOT NULL,
                                                 earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (achievement_id) REFERENCES achievements(id)
    );

CREATE TABLE IF NOT EXISTS streaks (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       user_id BIGINT NOT NULL UNIQUE,
                                       current_streak INT DEFAULT 0,
                                       last_login DATE,
                                       FOREIGN KEY (user_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS leaderboard_scores (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  user_id BIGINT NOT NULL,
                                                  game_type VARCHAR(50),
    score INT DEFAULT 0,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
    );




CREATE TABLE IF NOT EXISTS announcements (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    admin_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS challenges (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    reward TEXT NOT NULL,
    admin_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS games (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    max_players INT DEFAULT 2,
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS game_sessions (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             game_id BIGINT NOT NULL,
                                             host_id BIGINT NOT NULL,
                                             room_code VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(30) DEFAULT 'WAITING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (host_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS game_invites (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            session_id BIGINT NOT NULL,
                                            sender_id BIGINT NOT NULL,
                                            receiver_id BIGINT NOT NULL,
                                            status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, ACCEPTED, REJECTED, EXPIRED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS user_game_time (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                game_title VARCHAR(255) NOT NULL,
                                total_time_seconds BIGINT NOT NULL DEFAULT 0,
                                CONSTRAINT fk_user_game_time_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sudoku_puzzles (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              puzzle_date DATE UNIQUE, -- only one per day
                                              definition VARCHAR(81) NOT NULL,
                                              solution VARCHAR(81) NOT NULL,
                                              difficulty VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS game_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    context_id BIGINT,
    score_value DOUBLE NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES games(id)
);

CREATE TABLE IF NOT EXISTS game_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    targ_record_id BIGINT NOT NULL,
    res_record_id BIGINT,
    winner_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    FOREIGN KEY (targ_record_id) REFERENCES game_records(id),
    FOREIGN KEY (res_record_id) REFERENCES  game_records(id),
    FOREIGN KEY (winner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS wordle_puzzles(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    puzzle_date DATE UNIQUE,
    answer_word VARCHAR(5) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wordle_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    puzzle_id BIGINT NOT NULL,
    guesses VARCHAR(64) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (puzzle_id) REFERENCES wordle_puzzles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_puzzle (user_id, puzzle_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT IGNORE INTO games(title, description, max_players, icon_url, created_at)
VALUES (
        'WORDLE',
        'Guess 5 letter Georgian word in 6 tries',
        1,
        '/images/games/wordle.png',
        CURRENT_TIMESTAMP
       );
INSERT IGNORE INTO games(title, description, max_players, icon_url, created_at)
VALUES (
        'JOKER',
        'Georgian trick-taking card game with bidding',
        6,
        '/images/games/joker.png',
        CURRENT_TIMESTAMP
       );

CREATE TABLE IF NOT EXISTS joker_sessions (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              room_code VARCHAR(20) NOT NULL UNIQUE,  -- join key
                                              host_id BIGINT NOT NULL,                  -- who created the game
                                              status VARCHAR(30) DEFAULT 'WAITING',
                                              player_count INT NOT NULL,
                                              total_rounds INT NOT NULL,
                                              is_open BOOLEAN DEFAULT FALSE,
                                              joker_amount INT DEFAULT 1,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              ended_at TIMESTAMP NULL,
                                              FOREIGN KEY (host_id) REFERENCES users(id)
);