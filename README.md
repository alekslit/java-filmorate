# java-filmorate
Template repository for Filmorate project.

## База данных приложения Filmorate.

ER-диаграмма базы данных:

![ER-diagram of the Filmorate application database.](er_diagram.jpg)

Примеры SQL запросов к БД:

1. Получение списка всех фильмов:
```
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       f.rate,
       mr.mpa_rating_id,
       mr.name AS mpa_name,
       g.genre_id,
       g.name AS genre_name
FROM films AS f
LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id)
LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id)
LEFT OUTER JOIN genre AS g ON (fg.genre_id = g.genre_id);
```
2. Получение списка всех пользователей:
```
SELECT *
FROM users;
```
3. Получение фильма по id (в примере ищем фильм с id = 1):
```
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       f.rate,
       mr.mpa_rating_id,
       mr.name AS mpa_name,
       g.genre_id,
       g.name AS genre_name
FROM films AS f
LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id)
LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id)
LEFT OUTER JOIN genre AS g ON (fg.genre_id = g.genre_id)
WHERE film_id = 1;
```
4. Получение пользователя по id (в примере ищем пользователя с id = 1):
```
SELECT *
FROM users
WHERE user_id = 1;
```
5. Получаем список топ фильмов по количеству лайков (в примере ищем топ 10 фильмов):
```
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       f.rate,
       mr.mpa_rating_id,
       mr.name AS mpa_name,
       g.genre_id,
       g.name AS genre_name
FROM films AS f
LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id)
LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id)
LEFT OUTER JOIN genre AS g ON (fg.genre_id = g.genre_id)
ORDER BY f.rate DESC
LIMIT 10;
```
6. Получаем список друзей пользователя (например пользователя с id = 1):
```
SELECT *
FROM users
WHERE user_id IN (SELECT friend_id
                  FROM user_friendship
                  WHERE user_id = 1);
```
7. Получаем список общих друзей двух пользователей (id пользователей 1 и 2):
```
SELECT *
FROM users
WHERE user_id IN (SELECT friend_id
                  FROM user_friendship
                  WHERE user_id = 1
                    AND friend_id IN (SELECT friend_id
                                      FROM user_friendship
                                      WHERE user_id = 2));
```
8. Получение жанра фильма по id (в примере ищем жанр с id = 1):
```
SELECT *
FROM genre " +
WHERE genre_id = 1;
```
9. Получение списка всех жанров:
```
SELECT *
FROM genre;
```
10. Получение MPA-рейтинга по id (в примере ищем MPA-рейтинг с id = 1):
```
SELECT *
FROM mpa_rating
WHERE mpa_rating_id = 1;
```
11. Получение списка всех MPA-жанров:
```
"SELECT *
FROM mpa_rating;
```