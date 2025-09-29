-- テスト用データ
INSERT INTO message (id, text) VALUES (1, 'テストメッセージです！');
INSERT INTO message (id, text) VALUES (2, 'Spockテストが動作しています');
INSERT INTO message (id, text) VALUES (3, 'Spring BootとGroovyの統合成功');

-- テスト用ユーザーデータ
INSERT INTO users (id, username, password, roles, enabled) VALUES (1, 'user', '$2a$10$XY0d9WX4.TfZPJnYVN7/6OK5m8q0T8qZ8EfE6DZk6Fq1BT5v0vQ8G', 'USER', true);
INSERT INTO users (id, username, password, roles, enabled) VALUES (2, 'admin', '$2a$10$X/k1xhGK2sO5Qs7Eo3QbFu2oI5d2Bl7zG4iC5mA8fM3kN6pD9oK8Q', 'ADMIN,USER', true);
