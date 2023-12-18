-- MPA рейтинг:
MERGE INTO mpa_rating (mpa_rating_id, name)
VALUES(1, 'G');
MERGE INTO mpa_rating (mpa_rating_id, name)
VALUES(2, 'PG');
MERGE INTO mpa_rating (mpa_rating_id, name)
VALUES(3, 'PG-13');
MERGE INTO mpa_rating (mpa_rating_id, name)
VALUES(4, 'R');
MERGE INTO mpa_rating (mpa_rating_id, name)
VALUES(5, 'NC-17');

-- жанры фильмов:
MERGE INTO genre (genre_id, name)
VALUES(1, 'Комедия');
MERGE INTO genre (genre_id, name)
VALUES(2, 'Драма');
MERGE INTO genre (genre_id, name)
VALUES(3, 'Мультфильм');
MERGE INTO genre (genre_id, name)
VALUES(4, 'Триллер');
MERGE INTO genre (genre_id, name)
VALUES(5, 'Документальный');
MERGE INTO genre (genre_id, name)
VALUES(6, 'Боевик');