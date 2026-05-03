-- =========================================================================
--  PDM_Database_Group9.sql
--  IT079IU — Principles of Database Management — Final Project (Group 9)
--  Topic: Video Streaming Database (StreamFlix)
--
--  This file consolidates the three SQL artefacts of the project:
--    PART 1 -- Schema (DDL): tables, constraints, indexes, triggers, views
--    PART 2 -- Sample data (DML): seed rows for every table
--    PART 3 -- Queries: 25 SELECT queries, basic to advanced
--
--  How to load:
--    mysql -u root -p < PDM_Database_Group9.sql
--    -- or, if the database is already created:
--    mysql -u root -p streamflix_db < PDM_Database_Group9.sql
--
--  Built-in BCrypt password for all 10 demo users: "password123"
-- =========================================================================

-- =========================================================================
--  PART 1 -- Schema (DDL)
-- =========================================================================

-- =====================================================================
-- StreamFlix - Video Streaming Database Schema
-- Principles of Database Management (IT079IU)
-- Database: MySQL 8.0+
-- Normalization: 3NF / BCNF where applicable
-- =====================================================================

DROP DATABASE IF EXISTS streamflix_db;
CREATE DATABASE streamflix_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE streamflix_db;

-- =====================================================================
-- 1. USER PROFILES AND AUTHENTICATION
-- =====================================================================

-- Subscription plans (Basic, Premium, Family, etc.)
CREATE TABLE subscription_plan (
    plan_id       INT AUTO_INCREMENT PRIMARY KEY,
    plan_name     VARCHAR(50)   NOT NULL UNIQUE,
    price         DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    max_quality   VARCHAR(10)   NOT NULL DEFAULT 'HD',    -- SD, HD, FHD, 4K
    max_devices   INT           NOT NULL DEFAULT 1 CHECK (max_devices >= 1),
    description   TEXT
) ENGINE=InnoDB;

-- Users of the platform
CREATE TABLE app_user (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100),
    date_of_birth   DATE,
    country         VARCHAR(50),
    avatar_url      VARCHAR(500),
    role            ENUM('USER','CREATOR','ADMIN') DEFAULT 'USER',
    is_active       BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP    NULL,
    CONSTRAINT chk_email CHECK (email LIKE '%_@_%._%')
) ENGINE=InnoDB;

-- User's current subscription to a plan (1 user : N subscription records over time)
CREATE TABLE user_subscription (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    plan_id         INT    NOT NULL,
    start_date      DATE   NOT NULL,
    end_date        DATE   NOT NULL,
    status          ENUM('ACTIVE','EXPIRED','CANCELLED') DEFAULT 'ACTIVE',
    auto_renew      BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plan(plan_id) ON DELETE RESTRICT,
    CONSTRAINT chk_dates CHECK (end_date > start_date)
) ENGINE=InnoDB;

-- Payments recorded per subscription
CREATE TABLE payment (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    payment_date    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    method          ENUM('CREDIT_CARD','PAYPAL','BANK_TRANSFER','MOMO','ZALOPAY') NOT NULL,
    status          ENUM('PENDING','COMPLETED','FAILED','REFUNDED') DEFAULT 'PENDING',
    transaction_ref VARCHAR(100) UNIQUE,
    FOREIGN KEY (subscription_id) REFERENCES user_subscription(subscription_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 2. CONTENT MANAGEMENT (Channels, Categories, Videos)
-- =====================================================================

-- A channel is owned by a user (creator). 1:1 in this simplified model
CREATE TABLE channel (
    channel_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id     BIGINT NOT NULL UNIQUE,            -- one channel per user
    channel_name      VARCHAR(100) NOT NULL,
    description       TEXT,
    banner_url        VARCHAR(500),
    subscriber_count  BIGINT DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Category (Music, Gaming, Education...)
CREATE TABLE category (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(255)
) ENGINE=InnoDB;

-- Video: the main entity of the platform
CREATE TABLE video (
    video_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id      BIGINT       NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    video_url       VARCHAR(500) NOT NULL,     -- link to storage (S3/CDN)
    thumbnail_url   VARCHAR(500),
    duration_sec    INT          NOT NULL CHECK (duration_sec > 0),
    resolution      VARCHAR(10)  DEFAULT 'HD', -- SD, HD, FHD, 4K
    views_count     BIGINT       DEFAULT 0,
    likes_count     BIGINT       DEFAULT 0,
    dislikes_count  BIGINT       DEFAULT 0,
    status          ENUM('DRAFT','PUBLISHED','PRIVATE','REMOVED') DEFAULT 'PUBLISHED',
    is_premium      BOOLEAN      DEFAULT FALSE,   -- premium content requires subscription
    upload_date     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES channel(channel_id) ON DELETE CASCADE,
    INDEX idx_video_upload (upload_date DESC),
    INDEX idx_video_views  (views_count DESC),
    FULLTEXT INDEX ft_video_text (title, description)
) ENGINE=InnoDB;

-- M:N - a video can be in multiple categories
CREATE TABLE video_category (
    video_id     BIGINT NOT NULL,
    category_id  INT    NOT NULL,
    PRIMARY KEY (video_id, category_id),
    FOREIGN KEY (video_id)    REFERENCES video(video_id)       ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tags allow finer search
CREATE TABLE tag (
    tag_id   INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE video_tag (
    video_id BIGINT NOT NULL,
    tag_id   INT    NOT NULL,
    PRIMARY KEY (video_id, tag_id),
    FOREIGN KEY (video_id) REFERENCES video(video_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id)   REFERENCES tag(tag_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 3. USER INTERACTION
-- =====================================================================

-- A user subscribes to a channel (not the platform plan)
CREATE TABLE channel_subscription (
    subscriber_user_id BIGINT NOT NULL,
    channel_id         BIGINT NOT NULL,
    subscribed_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notifications_on   BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (subscriber_user_id, channel_id),
    FOREIGN KEY (subscriber_user_id) REFERENCES app_user(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (channel_id)         REFERENCES channel(channel_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Like or dislike on a video
CREATE TABLE video_reaction (
    user_id    BIGINT NOT NULL,
    video_id   BIGINT NOT NULL,
    reaction   ENUM('LIKE','DISLIKE') NOT NULL,
    reacted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, video_id),
    FOREIGN KEY (user_id)  REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES video(video_id)   ON DELETE CASCADE
) ENGINE=InnoDB;

-- Comments (supports nested replies via parent_comment_id)
CREATE TABLE comment (
    comment_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id          BIGINT NOT NULL,
    user_id           BIGINT NOT NULL,
    parent_comment_id BIGINT NULL,
    content           TEXT   NOT NULL,
    likes_count       INT    DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id)          REFERENCES video(video_id)   ON DELETE CASCADE,
    FOREIGN KEY (user_id)           REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comment(comment_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Watch history: every play session
CREATE TABLE watch_history (
    history_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    video_id       BIGINT NOT NULL,
    watched_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    watch_duration INT      NOT NULL DEFAULT 0,    -- seconds actually watched
    progress_pct   DECIMAL(5,2) DEFAULT 0.00,       -- 0.00-100.00
    device_type    VARCHAR(30),                      -- WEB, iOS, ANDROID, TV
    FOREIGN KEY (user_id)  REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES video(video_id)   ON DELETE CASCADE,
    INDEX idx_history_user_time (user_id, watched_at DESC)
) ENGINE=InnoDB;

-- Playlists
CREATE TABLE playlist (
    playlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    is_public   BOOLEAN  DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE playlist_video (
    playlist_id BIGINT NOT NULL,
    video_id    BIGINT NOT NULL,
    position    INT    NOT NULL,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_id, video_id),
    FOREIGN KEY (playlist_id) REFERENCES playlist(playlist_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id)    REFERENCES video(video_id)       ON DELETE CASCADE,
    UNIQUE KEY uk_playlist_position (playlist_id, position)
) ENGINE=InnoDB;

-- Notifications (new video from subscribed channel, reply to comment, etc.)
CREATE TABLE notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            ENUM('NEW_VIDEO','COMMENT_REPLY','SUB_EXPIRING','SYSTEM') NOT NULL,
    content         VARCHAR(500) NOT NULL,
    link_url        VARCHAR(500),
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read, created_at DESC)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. TRIGGERS - keep denormalized counters in sync
-- =====================================================================

DELIMITER $$

-- When a view is recorded, increment the counter on the video
CREATE TRIGGER trg_after_watch_insert
AFTER INSERT ON watch_history
FOR EACH ROW
BEGIN
    UPDATE video
       SET views_count = views_count + 1
     WHERE video_id = NEW.video_id;
END$$

-- Increment/decrement like & dislike counters on video
CREATE TRIGGER trg_after_reaction_insert
AFTER INSERT ON video_reaction
FOR EACH ROW
BEGIN
    IF NEW.reaction = 'LIKE' THEN
        UPDATE video SET likes_count = likes_count + 1 WHERE video_id = NEW.video_id;
    ELSE
        UPDATE video SET dislikes_count = dislikes_count + 1 WHERE video_id = NEW.video_id;
    END IF;
END$$

CREATE TRIGGER trg_after_reaction_delete
AFTER DELETE ON video_reaction
FOR EACH ROW
BEGIN
    IF OLD.reaction = 'LIKE' THEN
        UPDATE video SET likes_count = GREATEST(likes_count - 1, 0) WHERE video_id = OLD.video_id;
    ELSE
        UPDATE video SET dislikes_count = GREATEST(dislikes_count - 1, 0) WHERE video_id = OLD.video_id;
    END IF;
END$$

-- Maintain channel subscriber_count
CREATE TRIGGER trg_after_sub_insert
AFTER INSERT ON channel_subscription
FOR EACH ROW
BEGIN
    UPDATE channel
       SET subscriber_count = subscriber_count + 1
     WHERE channel_id = NEW.channel_id;
END$$

CREATE TRIGGER trg_after_sub_delete
AFTER DELETE ON channel_subscription
FOR EACH ROW
BEGIN
    UPDATE channel
       SET subscriber_count = GREATEST(subscriber_count - 1, 0)
     WHERE channel_id = OLD.channel_id;
END$$

DELIMITER ;

-- =====================================================================
-- 5. VIEWS - useful read-only projections for the recommendation engine
-- =====================================================================

-- Trending videos: high views in the last 7 days
CREATE OR REPLACE VIEW vw_trending_videos AS
SELECT v.video_id,
       v.title,
       c.channel_name,
       v.views_count,
       v.likes_count,
       v.upload_date,
       DATEDIFF(NOW(), v.upload_date) AS days_since_upload
  FROM video v
  JOIN channel c ON c.channel_id = v.channel_id
 WHERE v.status = 'PUBLISHED'
   AND v.upload_date >= DATE_SUB(NOW(), INTERVAL 7 DAY);

-- Public channel overview with latest activity
CREATE OR REPLACE VIEW vw_channel_stats AS
SELECT c.channel_id,
       c.channel_name,
       u.username              AS owner,
       c.subscriber_count,
       COUNT(v.video_id)       AS total_videos,
       COALESCE(SUM(v.views_count),0) AS total_views,
       MAX(v.upload_date)      AS last_upload
  FROM channel c
  JOIN app_user u  ON u.user_id = c.owner_user_id
  LEFT JOIN video v ON v.channel_id = c.channel_id AND v.status = 'PUBLISHED'
 GROUP BY c.channel_id, c.channel_name, u.username, c.subscriber_count;


-- =========================================================================
--  PART 2 -- Sample data (DML)
-- =========================================================================

-- =====================================================================
-- StreamFlix - Sample Data
-- Run AFTER 01_schema.sql
-- =====================================================================

USE streamflix_db;

-- 1. Subscription plans ------------------------------------------------
INSERT INTO subscription_plan (plan_name, price, max_quality, max_devices, description) VALUES
('Free',     0.00,  'SD',  1, 'Ad-supported, SD only'),
('Basic',    4.99,  'HD',  1, 'HD streaming on 1 device'),
('Standard', 9.99,  'FHD', 2, 'Full HD on up to 2 devices'),
('Premium', 15.99,  '4K',  4, '4K Ultra HD on up to 4 devices'),
('Family',  19.99,  '4K',  6, 'Shared 4K for up to 6 family members');

-- 2. Users -------------------------------------------------------------
-- 2. App users (10 accounts: 3 creators, 6 viewers, 1 admin) ----------
-- All accounts share the demo password "password123"
-- (BCrypt-12 hash below; matches the BCryptPasswordEncoder used by Spring Security)
INSERT INTO app_user (username, email, password_hash, full_name, date_of_birth, country, role) VALUES
('alice',   'alice@mail.com',   '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Alice Nguyen',   '1998-03-12', 'Vietnam', 'CREATOR'),
('bob',     'bob@mail.com',     '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Bob Tran',       '1995-07-22', 'Vietnam', 'CREATOR'),
('charlie', 'charlie@mail.com', '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Charlie Pham',   '2000-11-05', 'USA',     'CREATOR'),
('david',   'david@mail.com',   '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'David Le',       '2001-05-30', 'Vietnam', 'USER'),
('eve',     'eve@mail.com',     '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Eve Hoang',      '1999-09-14', 'Singapore','USER'),
('frank',   'frank@mail.com',   '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Frank Vo',       '2002-02-18', 'Vietnam', 'USER'),
('grace',   'grace@mail.com',   '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Grace Do',       '1997-12-01', 'USA',     'USER'),
('henry',   'henry@mail.com',   '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Henry Bui',      '1996-04-25', 'Vietnam', 'USER'),
('ivy',     'ivy@mail.com',     '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'Ivy Lam',        '2003-08-08', 'Japan',   'USER'),
('admin',   'admin@mail.com',   '$2a$12$i63Xtd/TLPqCY82wiKDpvuMH5iEQCEw7IvD8XEneXXoe7SJHFuVzm', 'System Admin',   '1990-01-01', 'Vietnam', 'ADMIN');

-- 3. User subscriptions (platform plans) -------------------------------
INSERT INTO user_subscription (user_id, plan_id, start_date, end_date, status) VALUES
(1, 4, '2026-01-01', '2026-12-31', 'ACTIVE'),    -- alice, Premium
(2, 3, '2026-02-01', '2026-12-31', 'ACTIVE'),    -- bob,   Standard
(3, 4, '2025-06-15', '2026-06-15', 'ACTIVE'),    -- charlie, Premium
(4, 2, '2026-03-01', '2026-09-01', 'ACTIVE'),    -- david, Basic
(5, 5, '2026-01-15', '2027-01-15', 'ACTIVE'),    -- eve, Family
(6, 1, '2026-01-01', '2027-01-01', 'ACTIVE'),    -- frank, Free
(7, 3, '2025-12-01', '2026-06-01', 'EXPIRED'),   -- grace, expired Standard
(8, 2, '2026-04-01', '2026-10-01', 'ACTIVE'),    -- henry, Basic
(9, 1, '2026-02-01', '2027-02-01', 'ACTIVE');    -- ivy, Free

-- 4. Payments ----------------------------------------------------------
INSERT INTO payment (subscription_id, amount, method, status, transaction_ref) VALUES
(1, 15.99, 'CREDIT_CARD',   'COMPLETED', 'TXN-0001'),
(2,  9.99, 'PAYPAL',        'COMPLETED', 'TXN-0002'),
(3, 15.99, 'CREDIT_CARD',   'COMPLETED', 'TXN-0003'),
(4,  4.99, 'MOMO',          'COMPLETED', 'TXN-0004'),
(5, 19.99, 'BANK_TRANSFER', 'COMPLETED', 'TXN-0005'),
(7,  9.99, 'PAYPAL',        'REFUNDED',  'TXN-0007'),
(8,  4.99, 'ZALOPAY',       'COMPLETED', 'TXN-0008');

-- 5. Channels (one per creator-user) -----------------------------------
INSERT INTO channel (owner_user_id, channel_name, description, subscriber_count) VALUES
(1, 'Alice Cooks',     'Vietnamese & Asian cuisine tutorials',         0),
(2, 'Bob Tech Review', 'Latest gadget reviews and tech news',          0),
(3, 'Charlie Gaming',  'Playthroughs, esports & gaming tutorials',     0);

-- 6. Categories --------------------------------------------------------
INSERT INTO category (name, description) VALUES
('Music',        'Music videos, concerts, and playlists'),
('Gaming',       'Game reviews, playthroughs and esports'),
('Education',    'Tutorials, lectures, and documentaries'),
('Food',         'Cooking shows and food reviews'),
('Tech',         'Technology reviews, tutorials and news'),
('Entertainment','Movies, TV series, and shows'),
('Sports',       'Sports highlights and analysis'),
('News',         'Current events and news reports');

-- 7. Tags --------------------------------------------------------------
INSERT INTO tag (name) VALUES
('tutorial'), ('review'), ('beginner'), ('advanced'), ('vietnamese'),
('pho'),      ('iphone'), ('android'),  ('fps'),      ('rpg'),
('vlog'),     ('recipe'), ('unboxing'), ('esports');

-- 8. Videos ------------------------------------------------------------
INSERT INTO video (channel_id, title, description, video_url, thumbnail_url, duration_sec, resolution, status, is_premium, upload_date) VALUES
-- Alice Cooks
(1, 'How to make Pho Bo at home',       'Step-by-step traditional beef pho recipe', 'https://cdn.streamflix/v/1.mp4', 'https://cdn.streamflix/t/1.jpg',  900, 'FHD', 'PUBLISHED', FALSE, '2026-03-01 10:00:00'),
(1, 'Banh Mi: 3 Easy Variations',       'Classic, vegan and seafood banh mi',       'https://cdn.streamflix/v/2.mp4', 'https://cdn.streamflix/t/2.jpg',  720, 'FHD', 'PUBLISHED', FALSE, '2026-03-15 09:30:00'),
(1, 'Masterclass: Dumplings 101',       'Pro techniques for dumpling making',       'https://cdn.streamflix/v/3.mp4', 'https://cdn.streamflix/t/3.jpg', 1800, '4K',  'PUBLISHED', TRUE,  '2026-04-01 11:00:00'),
-- Bob Tech Review
(2, 'iPhone 17 Pro Review',             'In-depth review of the iPhone 17 Pro',     'https://cdn.streamflix/v/4.mp4', 'https://cdn.streamflix/t/4.jpg', 1500, '4K',  'PUBLISHED', FALSE, '2026-03-10 15:00:00'),
(2, 'Samsung S26 vs iPhone 17',         'Flagship comparison 2026',                  'https://cdn.streamflix/v/5.mp4', 'https://cdn.streamflix/t/5.jpg', 1200, 'FHD', 'PUBLISHED', FALSE, '2026-04-05 14:00:00'),
(2, 'Best Laptops under $1000 (2026)',  'Budget laptop roundup',                     'https://cdn.streamflix/v/6.mp4', 'https://cdn.streamflix/t/6.jpg', 1050, 'FHD', 'PUBLISHED', FALSE, '2026-04-12 10:00:00'),
-- Charlie Gaming
(3, 'Elden Ring 2 - Full Walkthrough',  'No-commentary full playthrough',            'https://cdn.streamflix/v/7.mp4', 'https://cdn.streamflix/t/7.jpg', 3600, '4K',  'PUBLISHED', TRUE,  '2026-02-20 20:00:00'),
(3, 'Valorant Ranked Tips 2026',        'Climb from Bronze to Diamond',              'https://cdn.streamflix/v/8.mp4', 'https://cdn.streamflix/t/8.jpg',  900, 'FHD', 'PUBLISHED', FALSE, '2026-04-10 19:00:00'),
(3, 'Top 10 Indie Games of 2026',       'Underrated indie gems',                     'https://cdn.streamflix/v/9.mp4', 'https://cdn.streamflix/t/9.jpg',  780, 'FHD', 'PUBLISHED', FALSE, '2026-04-15 18:00:00'),
(3, 'Pro FPS Aim Training (Draft)',     'Draft - not published yet',                 'https://cdn.streamflix/v/10.mp4','https://cdn.streamflix/t/10.jpg', 600, 'FHD', 'DRAFT',     FALSE, '2026-04-18 12:00:00');

-- 9. Video <-> Category --------------------------------------------------
INSERT INTO video_category (video_id, category_id) VALUES
(1,4),(2,4),(3,4),(3,3),
(4,5),(5,5),(6,5),(6,3),
(7,2),(7,6),(8,2),(8,3),(9,2);

-- 10. Video <-> Tag -----------------------------------------------------
INSERT INTO video_tag (video_id, tag_id) VALUES
(1,5),(1,6),(1,12),
(2,5),(2,12),
(3,1),(3,4),(3,12),
(4,2),(4,7),(4,13),
(5,2),(5,7),(5,8),
(6,2),(6,3),
(7,10),
(8,1),(8,9),(8,14),
(9,2);

-- 11. Channel subscriptions (users -> channels) ------------------------
INSERT INTO channel_subscription (subscriber_user_id, channel_id) VALUES
(4,1),(4,2),(4,3),
(5,1),(5,3),
(6,2),(6,3),
(7,1),(7,2),
(8,3),
(9,1),(9,2),(9,3);

-- 12. Reactions --------------------------------------------------------
INSERT INTO video_reaction (user_id, video_id, reaction) VALUES
(4,1,'LIKE'),(5,1,'LIKE'),(6,1,'LIKE'),(7,1,'DISLIKE'),
(4,2,'LIKE'),(5,2,'LIKE'),
(4,4,'LIKE'),(6,4,'LIKE'),(7,4,'LIKE'),(9,4,'LIKE'),
(5,5,'LIKE'),(8,5,'DISLIKE'),
(4,7,'LIKE'),(6,7,'LIKE'),(8,7,'LIKE'),(9,7,'LIKE'),
(4,8,'LIKE'),(6,8,'LIKE'),
(4,9,'DISLIKE');

-- 13. Comments ---------------------------------------------------------
INSERT INTO comment (video_id, user_id, parent_comment_id, content) VALUES
(1, 4, NULL, 'Amazing recipe! Tried it tonight.'),
(1, 5, NULL, 'My family loved this, thanks Alice!'),
(1, 6, 1,    'Agree, the broth was perfect.'),
(4, 7, NULL, 'Best iPhone review so far.'),
(4, 8, NULL, 'Battery test inaccurate though.'),
(4, 2, 5,    'Noted - I will retest in next video.'),
(7, 6, NULL, 'Elden Ring 2 is masterpiece.'),
(7, 8, NULL, 'Part 3 boss - impossible.'),
(8, 4, NULL, 'Climbed to Platinum after this, thanks!');

-- 14. Watch history ----------------------------------------------------
INSERT INTO watch_history (user_id, video_id, watch_duration, progress_pct, device_type, watched_at) VALUES
(4, 1,  900, 100.00, 'WEB',     '2026-04-01 20:10:00'),
(4, 4, 1500, 100.00, 'iOS',     '2026-04-02 21:00:00'),
(4, 7, 1800,  50.00, 'TV',      '2026-04-03 22:00:00'),
(5, 1,  450,  50.00, 'ANDROID', '2026-04-05 19:00:00'),
(5, 2,  720, 100.00, 'ANDROID', '2026-04-06 20:00:00'),
(6, 4,  750,  50.00, 'WEB',     '2026-04-07 10:00:00'),
(6, 7, 3600, 100.00, 'TV',      '2026-04-08 22:00:00'),
(7, 1,  300,  33.33, 'WEB',     '2026-04-09 18:00:00'),
(7, 4, 1500, 100.00, 'WEB',     '2026-04-10 19:00:00'),
(8, 7, 3600, 100.00, 'iOS',     '2026-04-11 21:00:00'),
(8, 8,  900, 100.00, 'iOS',     '2026-04-12 22:00:00'),
(9, 1,  900, 100.00, 'WEB',     '2026-04-13 09:00:00'),
(9, 4, 1500, 100.00, 'WEB',     '2026-04-14 10:00:00'),
(9, 7, 1800,  50.00, 'WEB',     '2026-04-15 11:00:00');

-- 15. Playlists --------------------------------------------------------
INSERT INTO playlist (user_id, title, description, is_public) VALUES
(4, 'My Favourite Recipes', 'Best cooking videos',   TRUE),
(4, 'Watch Later',           'Videos to watch soon', FALSE),
(6, 'Gaming Marathon',       'Long RPG playthroughs', TRUE);

INSERT INTO playlist_video (playlist_id, video_id, position) VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3),
(2, 4, 1), (2, 5, 2),
(3, 7, 1), (3, 9, 2);

-- 16. Notifications ----------------------------------------------------
INSERT INTO notification (user_id, type, content, link_url, is_read) VALUES
(4, 'NEW_VIDEO',     'Alice Cooks posted: Masterclass Dumplings 101', '/watch/3', FALSE),
(4, 'COMMENT_REPLY', 'Bob replied to your comment on iPhone 17 Pro Review', '/watch/4', FALSE),
(5, 'NEW_VIDEO',     'Charlie Gaming posted: Top 10 Indie Games 2026', '/watch/9', TRUE),
(7, 'SUB_EXPIRING',  'Your Standard plan expired',                     '/account', FALSE);


-- =========================================================================
--  PART 3 -- 25 demonstration queries (basic to advanced)
-- =========================================================================

-- =====================================================================
-- StreamFlix - Query Catalogue
-- Demonstrates: SELECT / JOIN / GROUP BY / HAVING / subqueries /
-- CTEs / window functions / set operations.
-- =====================================================================
USE streamflix_db;

-- ---------------------------------------------------------------------
-- BASIC QUERIES
-- ---------------------------------------------------------------------

-- Q1. List all published videos with their channel name
SELECT v.video_id, v.title, c.channel_name, v.views_count
FROM   video v
JOIN   channel c ON c.channel_id = v.channel_id
WHERE  v.status = 'PUBLISHED'
ORDER  BY v.upload_date DESC;

-- Q2. Users who registered from Vietnam
SELECT user_id, username, email, country
FROM   app_user
WHERE  country = 'Vietnam'
ORDER  BY created_at DESC;

-- Q3. Videos longer than 20 minutes, sorted by length
SELECT title, duration_sec, ROUND(duration_sec/60, 1) AS duration_min
FROM   video
WHERE  duration_sec > 1200
ORDER  BY duration_sec DESC;

-- Q4. Search videos containing a keyword (LIKE)
SELECT video_id, title
FROM   video
WHERE  title LIKE '%iPhone%' OR description LIKE '%iPhone%';

-- Q5. Full-text search (using FULLTEXT index)
SELECT video_id, title,
       MATCH(title, description) AGAINST ('iphone review' IN NATURAL LANGUAGE MODE) AS score
FROM   video
WHERE  MATCH(title, description) AGAINST ('iphone review' IN NATURAL LANGUAGE MODE)
ORDER  BY score DESC;


-- ---------------------------------------------------------------------
-- JOINS
-- ---------------------------------------------------------------------

-- Q6. Videos with every category they belong to (M:N join)
SELECT v.title, GROUP_CONCAT(cat.name ORDER BY cat.name SEPARATOR ', ') AS categories
FROM   video v
JOIN   video_category vc ON vc.video_id = v.video_id
JOIN   category cat      ON cat.category_id = vc.category_id
GROUP  BY v.video_id, v.title;

-- Q7. Channels and the list of users subscribed to them
SELECT c.channel_name, u.username AS subscriber
FROM   channel c
LEFT JOIN channel_subscription cs ON cs.channel_id = c.channel_id
LEFT JOIN app_user u              ON u.user_id = cs.subscriber_user_id
ORDER  BY c.channel_name, u.username;

-- Q8. Users who NEVER watched a video (LEFT JOIN + NULL check)
SELECT u.user_id, u.username
FROM   app_user u
LEFT JOIN watch_history h ON h.user_id = u.user_id
WHERE  h.history_id IS NULL
  AND  u.role <> 'ADMIN';


-- ---------------------------------------------------------------------
-- AGGREGATES / GROUP BY / HAVING
-- ---------------------------------------------------------------------

-- Q9. Videos per channel
SELECT c.channel_name, COUNT(v.video_id) AS total_videos
FROM   channel c
LEFT JOIN video v ON v.channel_id = c.channel_id
GROUP  BY c.channel_id, c.channel_name
ORDER  BY total_videos DESC;

-- Q10. Average watch duration per video (and % of total length)
SELECT v.title,
       v.duration_sec,
       ROUND(AVG(h.watch_duration), 0)                    AS avg_watched_sec,
       ROUND(AVG(h.watch_duration)*100/v.duration_sec, 1) AS avg_pct
FROM   video v
JOIN   watch_history h ON h.video_id = v.video_id
GROUP  BY v.video_id, v.title, v.duration_sec
ORDER  BY avg_pct DESC;

-- Q11. Channels with more than 2 videos AND total views > 0
SELECT c.channel_name,
       COUNT(v.video_id)  AS videos,
       SUM(v.views_count) AS total_views
FROM   channel c
JOIN   video v ON v.channel_id = c.channel_id
GROUP  BY c.channel_id, c.channel_name
HAVING COUNT(v.video_id) > 2
   AND SUM(v.views_count) > 0;

-- Q12. Daily new signup count in the last 30 days
SELECT DATE(created_at) AS day, COUNT(*) AS new_users
FROM   app_user
WHERE  created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP  BY DATE(created_at)
ORDER  BY day;


-- ---------------------------------------------------------------------
-- SUBQUERIES
-- ---------------------------------------------------------------------

-- Q13. Videos whose view count is above the average
SELECT title, views_count
FROM   video
WHERE  views_count > (SELECT AVG(views_count) FROM video);

-- Q14. Users who subscribed to EVERY channel (division)
SELECT u.username
FROM   app_user u
WHERE  NOT EXISTS (
          SELECT 1
          FROM   channel c
          WHERE  NOT EXISTS (
                    SELECT 1
                    FROM   channel_subscription cs
                    WHERE  cs.channel_id = c.channel_id
                      AND  cs.subscriber_user_id = u.user_id));

-- Q15. For every channel, find its most viewed video (correlated subquery)
SELECT c.channel_name, v.title, v.views_count
FROM   channel c
JOIN   video   v ON v.channel_id = c.channel_id
WHERE  v.views_count = (SELECT MAX(v2.views_count)
                          FROM   video v2
                          WHERE  v2.channel_id = c.channel_id);

-- Q16. Users who liked AT LEAST ONE video from channel 'Alice Cooks'
SELECT DISTINCT u.username
FROM   app_user u
WHERE  u.user_id IN (
          SELECT vr.user_id
          FROM   video_reaction vr
          JOIN   video v   ON v.video_id = vr.video_id
          JOIN   channel c ON c.channel_id = v.channel_id
          WHERE  c.channel_name = 'Alice Cooks'
            AND  vr.reaction   = 'LIKE');


-- ---------------------------------------------------------------------
-- SET OPERATIONS
-- ---------------------------------------------------------------------

-- Q17. Users who are creators OR have an active paid plan (UNION)
SELECT user_id, username, 'creator' AS reason FROM app_user WHERE role = 'CREATOR'
UNION
SELECT u.user_id, u.username, 'paid'
FROM   app_user u
JOIN   user_subscription us ON us.user_id = u.user_id
JOIN   subscription_plan sp ON sp.plan_id  = us.plan_id
WHERE  us.status = 'ACTIVE' AND sp.price > 0;

-- Q18. Users who watched videos but never commented (EXCEPT / NOT IN)
SELECT DISTINCT u.user_id, u.username
FROM   app_user u
JOIN   watch_history h ON h.user_id = u.user_id
WHERE  u.user_id NOT IN (SELECT user_id FROM comment);


-- ---------------------------------------------------------------------
-- CTEs AND WINDOW FUNCTIONS (advanced / analytics)
-- ---------------------------------------------------------------------

-- Q19. Top-3 most viewed videos per channel (CTE + ROW_NUMBER)
WITH ranked AS (
  SELECT v.channel_id, v.video_id, v.title, v.views_count,
         ROW_NUMBER() OVER (PARTITION BY v.channel_id
                            ORDER BY v.views_count DESC) AS rn
  FROM   video v
  WHERE  v.status = 'PUBLISHED'
)
SELECT c.channel_name, r.title, r.views_count, r.rn
FROM   ranked r
JOIN   channel c ON c.channel_id = r.channel_id
WHERE  r.rn <= 3
ORDER  BY c.channel_name, r.rn;

-- Q20. Running total of monthly revenue
WITH monthly AS (
  SELECT DATE_FORMAT(payment_date, '%Y-%m') AS ym,
         SUM(amount) AS monthly_revenue
  FROM   payment
  WHERE  status = 'COMPLETED'
  GROUP  BY DATE_FORMAT(payment_date, '%Y-%m')
)
SELECT ym,
       monthly_revenue,
       SUM(monthly_revenue) OVER (ORDER BY ym) AS cumulative_revenue
FROM   monthly;

-- Q21. Recommendation: similar users (users who watched the same video as user 4)
WITH u4_videos AS (
  SELECT DISTINCT video_id FROM watch_history WHERE user_id = 4
)
SELECT u.user_id, u.username,
       COUNT(*) AS shared_videos
FROM   watch_history h
JOIN   app_user u ON u.user_id = h.user_id
WHERE  h.video_id IN (SELECT video_id FROM u4_videos)
  AND  h.user_id  <> 4
GROUP  BY u.user_id, u.username
ORDER  BY shared_videos DESC
LIMIT  5;

-- Q22. Retention: of users that signed up in Jan 2026, how many are still active?
SELECT COUNT(DISTINCT u.user_id) AS jan_signups,
       COUNT(DISTINCT CASE WHEN us.status='ACTIVE' THEN u.user_id END) AS still_active
FROM   app_user u
LEFT JOIN user_subscription us ON us.user_id = u.user_id
WHERE  u.created_at BETWEEN '2026-01-01' AND '2026-01-31';

-- Q23. Revenue by plan and country
SELECT u.country, sp.plan_name, SUM(p.amount) AS revenue
FROM   payment p
JOIN   user_subscription us ON us.subscription_id = p.subscription_id
JOIN   subscription_plan sp ON sp.plan_id = us.plan_id
JOIN   app_user u ON u.user_id = us.user_id
WHERE  p.status = 'COMPLETED'
GROUP  BY u.country, sp.plan_name WITH ROLLUP;

-- Q24. Engagement score per video: views + 2*likes - dislikes
SELECT v.video_id, v.title,
       v.views_count,
       v.likes_count,
       v.dislikes_count,
       (v.views_count + 2 * v.likes_count - v.dislikes_count) AS engagement_score
FROM   video v
WHERE  v.status = 'PUBLISHED'
ORDER  BY engagement_score DESC
LIMIT  10;

-- Q25. Find users who reached EVERY category (watched at least 1 video in each)
-- Relational division using double-negation
SELECT u.user_id, u.username
FROM   app_user u
WHERE  NOT EXISTS (
    SELECT 1 FROM category cat
    WHERE NOT EXISTS (
        SELECT 1
        FROM watch_history h
        JOIN video_category vc ON vc.video_id = h.video_id
        WHERE h.user_id = u.user_id
          AND vc.category_id = cat.category_id));
