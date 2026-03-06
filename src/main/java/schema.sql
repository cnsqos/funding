-- =========================================
-- FUNDING FINAL SCHEMA (H2 DEV VERSION)
-- =========================================

DROP ALL OBJECTS;

-- =========================
-- 1) USERS
-- =========================
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  nickname VARCHAR(100) NOT NULL,
  password VARCHAR(255),
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  provider VARCHAR(20) NOT NULL,
  provider_id VARCHAR(255),
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  email_verified_at TIMESTAMP,
  profile_image VARCHAR(500),
  last_login_at TIMESTAMP,
  suspended_reason VARCHAR(255),
  deleted_reason VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT uq_users_email UNIQUE (email),
  CONSTRAINT uq_users_nickname UNIQUE (nickname)
);

CREATE INDEX idx_users_email ON users(email);

-- =========================
-- 2) PASSWORD RESET TOKENS
-- =========================
CREATE TABLE password_reset_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  expired_at TIMESTAMP NOT NULL,
  used_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT uq_prt_token UNIQUE (token),
  CONSTRAINT fk_prt_user
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_prt_user_id ON password_reset_tokens(user_id);

-- =========================
-- 3) CATEGORIES
-- =========================
CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT uq_categories_name UNIQUE (name)
);

-- =========================
-- 4) PROJECTS
-- =========================
CREATE TABLE projects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content CLOB,
  goal_amount BIGINT NOT NULL,
  current_amount BIGINT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL,
  start_at TIMESTAMP,
  deadline TIMESTAMP NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,

  CONSTRAINT fk_projects_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_projects_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_projects_title ON projects(title);
CREATE INDEX idx_projects_category ON projects(category_id);
CREATE INDEX idx_projects_deadline ON projects(deadline);

-- =========================
-- 5) PROJECT REVIEWS
-- =========================
CREATE TABLE project_reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  requester_id BIGINT NOT NULL,
  admin_id BIGINT,
  status VARCHAR(20) NOT NULL,
  comment CLOB,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  processed_at TIMESTAMP,

  CONSTRAINT fk_pr_project
    FOREIGN KEY (project_id) REFERENCES projects(id),

  CONSTRAINT fk_pr_requester
    FOREIGN KEY (requester_id) REFERENCES users(id),

  CONSTRAINT fk_pr_admin
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

CREATE INDEX idx_pr_project_id ON project_reviews(project_id);
CREATE INDEX idx_pr_requester_id ON project_reviews(requester_id);
CREATE INDEX idx_pr_admin_id ON project_reviews(admin_id);

-- =========================
-- 6) PROJECT STATUS LOGS
-- =========================
CREATE TABLE project_status_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  before_status VARCHAR(30) NOT NULL,
  after_status VARCHAR(30) NOT NULL,
  changed_by VARCHAR(20) NOT NULL,
  changed_by_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_psl_project
    FOREIGN KEY (project_id) REFERENCES projects(id),

  CONSTRAINT fk_psl_user
    FOREIGN KEY (changed_by_id) REFERENCES users(id)
);

CREATE INDEX idx_psl_project_id ON project_status_logs(project_id);
CREATE INDEX idx_psl_changed_by_id ON project_status_logs(changed_by_id);

-- =========================
-- 7) DONATIONS
-- =========================
CREATE TABLE donations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  amount BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  cancel_deadline TIMESTAMP,
  refunded_at TIMESTAMP,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_donations_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_donations_project
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_donations_project_id ON donations(project_id);
CREATE INDEX idx_donations_user_id ON donations(user_id);

-- =========================
-- 8) LIKES
-- =========================
CREATE TABLE likes (
  user_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (user_id, project_id),

  CONSTRAINT fk_likes_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_likes_project
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- =========================
-- 9) FOLLOWS
-- =========================
CREATE TABLE follows (
  follower_id BIGINT NOT NULL,
  following_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (follower_id, following_id),

  CONSTRAINT fk_follows_follower
    FOREIGN KEY (follower_id) REFERENCES users(id),

  CONSTRAINT fk_follows_following
    FOREIGN KEY (following_id) REFERENCES users(id)
);

-- =========================
-- 10) TAGS
-- =========================
CREATE TABLE tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  normalized_name VARCHAR(100) NOT NULL,

  CONSTRAINT uq_tags_normalized UNIQUE (normalized_name)
);

-- =========================
-- 11) PROJECT_TAGS
-- =========================
CREATE TABLE project_tags (
  project_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,

  PRIMARY KEY (project_id, tag_id),

  CONSTRAINT fk_pt_project
    FOREIGN KEY (project_id) REFERENCES projects(id),

  CONSTRAINT fk_pt_tag
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);

-- =========================
-- 12) PROJECT IMAGES
-- =========================
CREATE TABLE project_images (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  is_thumbnail BOOLEAN NOT NULL DEFAULT FALSE,

  CONSTRAINT fk_pi_project
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_pi_project_id ON project_images(project_id);

-- =========================
-- 13) PROJECT FILES
-- =========================
CREATE TABLE project_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  file_size BIGINT NOT NULL,

  CONSTRAINT fk_pf_project
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_pf_project_id ON project_files(project_id);

-- =========================
-- 14) ADMIN ACTION LOGS
-- =========================
CREATE TABLE admin_action_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_id BIGINT NOT NULL,
  target_type VARCHAR(50) NOT NULL,
  target_id BIGINT NOT NULL,
  before_status VARCHAR(30),
  after_status VARCHAR(30),
  reason VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_aal_admin
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

CREATE INDEX idx_aal_admin_id ON admin_action_logs(admin_id);

-- =========================
-- 15) PROJECT DELETE REQUESTS
-- =========================
CREATE TABLE project_delete_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  requester_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  admin_id BIGINT,
  requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approved_at TIMESTAMP,

  CONSTRAINT fk_pdr_project
    FOREIGN KEY (project_id) REFERENCES projects(id),

  CONSTRAINT fk_pdr_requester
    FOREIGN KEY (requester_id) REFERENCES users(id),

  CONSTRAINT fk_pdr_admin
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

CREATE INDEX idx_pdr_project_id ON project_delete_requests(project_id);
CREATE INDEX idx_pdr_requester_id ON project_delete_requests(requester_id);
CREATE INDEX idx_pdr_admin_id ON project_delete_requests(admin_id);

-- =========================
-- 16) DAILY STATISTICS
-- =========================
CREATE TABLE daily_statistics (
  stat_date DATE PRIMARY KEY,
  total_donation_amount BIGINT NOT NULL DEFAULT 0,
  project_count INT NOT NULL DEFAULT 0,
  success_project_count INT NOT NULL DEFAULT 0,
  user_count INT NOT NULL DEFAULT 0
);

-- =========================
-- 17) PROJECT DAILY VIEWS
-- =========================
CREATE TABLE project_daily_views (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  view_date DATE NOT NULL,
  view_count INT NOT NULL DEFAULT 0,

  CONSTRAINT uq_pdv_project_date UNIQUE (project_id, view_date),

  CONSTRAINT fk_pdv_project
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- =========================
-- 18) EMAIL BLACKLISTS
-- =========================
CREATE TABLE email_blacklists (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  reason VARCHAR(255),
  banned_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT uq_eb_email UNIQUE (email),

  CONSTRAINT fk_eb_admin
    FOREIGN KEY (banned_by) REFERENCES users(id)
);

CREATE INDEX idx_eb_email ON email_blacklists(email);