package ru.yandex.practicum.filmorate.query;

// класс для хранения строк SQL запросов:
public class SqlQuery {
    /*----Запросы для объектов Film----*/
    public static final String SQL_QUERY_UPDATE_FILM =
            "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, rate = ?, mpa_rating_id = ? " +
            "WHERE film_id = ?;";

    public static final String SQL_QUERY_GET_ALL_FILMS =
            "SELECT f.film_id, " +
                   "f.name, " +
                   "f.description, " +
                   "f.release_date, " +
                   "f.duration, " +
                   "f.rate, " +
                   "mr.mpa_rating_id, " +
                   "mr.name AS mpa_name, " +
                   "g.genre_id, " +
                   "g.name AS genre_name " +
            "FROM films AS f " +
            "LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id) " +
            "LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id) " +
            "LEFT OUTER JOIN genre AS g ON (fg.genre_id = g.genre_id);";

    public static final String SQL_QUERY_GET_FILM_BY_ID =
            "SELECT f.film_id, " +
                   "f.name, " +
                   "f.description, " +
                   "f.release_date, " +
                   "f.duration, " +
                   "f.rate, " +
                   "mr.mpa_rating_id, " +
                   "mr.name AS mpa_name, " +
                   "g.genre_id, " +
                   "g.name AS genre_name " +
            "FROM films AS f " +
            "LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id) " +
            "LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id) " +
            "LEFT OUTER JOIN genre AS g ON (fg.genre_id = g.genre_id) " +
            "WHERE f.film_id = ?;";

    public static final String SQL_QUERY_REMOVE_LIKE_FROM_FILM =
            "DELETE FROM film_likes " +
            "WHERE film_id = ? " +
              "AND user_id = ?;";

    public static final String SQL_QUERY_GET_TOP_FILMS_FOR_LIKES =
            "SELECT f.film_id, " +
                   "f.name, " +
                   "f.description, " +
                   "f.release_date, " +
                   "f.duration, " +
                   "f.rate, " +
                   "mr.mpa_rating_id, " +
                   "mr.name AS mpa_name, " +
                   "g.genre_id, " +
                   "g.name AS genre_name " +
            "FROM films AS f " +
            "LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id) " +
            "LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id) " +
            "LEFT OUTER JOIN genre AS g ON (fg.genre_id = g.genre_id) " +
            "ORDER BY f.rate DESC " +
            "LIMIT ?;";

    public static final String SQL_QUERY_FILM_RATE_PLUS =
            "UPDATE films " +
            "SET rate = rate + 1 " +
            "WHERE film_id = ?;";

    public static final String SQL_QUERY_FILM_RATE_MINUS =
            "UPDATE films " +
            "SET rate = rate - 1 " +
            "WHERE film_id = ?;";

    /*----Запросы для объектов User----*/
    public static final String SQL_QUERY_UPDATE_USER =
            "UPDATE users SET " +
            "email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?;";

    public static final String SQL_QUERY_GET_ALL_USERS =
            "SELECT user_id, " +
                   "email, " +
                   "login, " +
                   "name, " +
                   "birthday " +
            "FROM users;";

    public static final String SQL_QUERY_GET_USER_BY_ID =
            "SELECT user_id, " +
                   "email, " +
                   "login, " +
                   "name, " +
                   "birthday " +
            "FROM users " +
            "WHERE user_id = ?;";

    public static final String SQL_QUERY_REMOVE_USER_FROM_FRIENDS =
            "DELETE FROM user_friendship " +
            "WHERE user_id = ? " +
              "AND friend_id = ?;";

    public static final String SQL_QUERY_GET_ALL_FRIEND_LIST =
            "SELECT u.user_id, " +
                   "u.email, " +
                   "u.login, " +
                   "u.name, " +
                   "u.birthday " +
            "FROM users AS u " +
            "JOIN user_friendship AS uf ON (u.user_id = uf.friend_id) " +
            "WHERE uf.user_id = ?;";

    public static final String SQL_QUERY_GET_COMMON_FRIENDS =
            "SELECT u.user_id, " +
                   "u.email, " +
                   "u.login, " +
                   "u.name, " +
                   "u.birthday " +
            "FROM users AS u " +
            "JOIN user_friendship AS uf1 ON (u.user_id = uf1.friend_id) " +
            "JOIN user_friendship AS uf2 ON (uf1.friend_id = uf2.friend_id) " +
            "WHERE uf1.user_id = ? " +
              "AND uf2.user_id = ?;";

    /*----Запросы для объектов Genre----*/
    public static final String SQL_QUERY_GET_GENRE_BY_ID =
            "SELECT * " +
            "FROM genre " +
            "WHERE genre_id = ?;";

    public static final String SQL_QUERY_GET_ALL_GENRES =
            "SELECT * " +
            "FROM genre;";

    /*----Запросы для объектов Mpa----*/
    public static final String SQL_QUERY_GET_MPA_BY_ID =
            "SELECT * " +
            "FROM mpa_rating " +
            "WHERE mpa_rating_id = ?;";

    public static final String SQL_QUERY_GET_ALL_MPA =
            "SELECT * " +
            "FROM mpa_rating;";
}