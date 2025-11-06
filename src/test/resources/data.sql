-- Очистка таблиц (важно для тестов)
DELETE FROM comments;
DELETE FROM bookings;
DELETE FROM items;
DELETE FROM users;

-- Тестовые пользователи
INSERT INTO users (id, name, email) VALUES
(1, 'Test User 1', 'test1@example.com'),
(2, 'Test User 2', 'test2@example.com');

-- Тестовые вещи
INSERT INTO items (id, name, description, is_available, owner_id) VALUES
(1, 'Test Item 1', 'Test Description 1', true, 1),
(2, 'Test Item 2', 'Test Description 2', true, 2);