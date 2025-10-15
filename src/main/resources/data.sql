DELETE FROM users;
-- user/password (BCrypt hash of "password")
INSERT INTO users (username, password, roles, enabled) VALUES
('user', '$2a$10$XY0d9WX4.TfZPJnYVN7/6OK5m8q0T8qZ8EfE6DZk6Fq1BT5v0vQ8G', 'USER', true);
INSERT INTO users (username, password, roles, enabled) VALUES
('admin', '$2a$10$ac1.Hor.5Q6r2vdRrQ9uxOo66w.G6YWJiXzArcGIkxKdzNKPrvQce', 'ADMIN,USER', true);
