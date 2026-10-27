DELETE FROM notifications;
DELETE FROM feedbacks;
DELETE FROM reports;
DELETE FROM loans;
DELETE FROM requests;
DELETE FROM books;
DELETE FROM authors;
DELETE FROM genres;
DELETE FROM users WHERE email NOT IN ('user@xlibris.com', 'moderator@xlibris.com', 'admin@xlibris.com');