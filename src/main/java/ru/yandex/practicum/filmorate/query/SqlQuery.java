package ru.yandex.practicum.filmorate.query;

// класс для хранения строк SQL запросов:
public class SqlQuery {
    /*----Запросы для объектов Film----*/
    public static final String SQL_QUERY_UPDATE_FILM =
            "UPDATE FILMS SET name = ?, " +
                    "release_date = ?, " +
                    "description = ?, " +
                    "duration = ?, " +
                    "mpa_rating_id = ? " +
                    "WHERE film_id = ?";

    public static final String SQL_QUERY_GET_ALL_FILMS =
            "SELECT f.film_id, " +
                    "f.name, " +
                    "f.release_date, " +
                    "f.description, " +
                    "f.duration, " +
                    "mp.mpa_rating_id, " +
                    "mp.name AS mpa_name " +
                    "FROM FILMS AS f " +
                    "JOIN mpa_rating AS mp ON f.mpa_rating_id = mp.mpa_rating_id;";

    public static final String SQL_QUERY_GET_FILM_BY_ID =
            "SELECT f.film_id, " +
                    "f.name, " +
                    "f.release_date, " +
                    "f.description, " +
                    "f.duration, " +
                    "mp.mpa_rating_id, " +
                    "mp.name AS mpa_name " +
                    "FROM FILMS AS f " +
                    "JOIN mpa_rating AS mp ON f.mpa_rating_id = mp.mpa_rating_id  " +
                    "WHERE f.film_id = ?;";

    public static final String SQL_QUERY_FILM_ADD_LIKE =
            "INSERT INTO film_likes(film_id, user_id) " +
                    "VALUES(?, ?);";
    public static final String SQL_QUERY_REMOVE_LIKE_FROM_FILM =
            "DELETE FROM film_likes " +
                    "WHERE user_id = ? " +
                    "AND film_id = ?";

    public static final String SQL_QUERY_GET_TOP_FILMS_FOR_LIKES =
            "SELECT f.film_id, " +
                    "f.name, " +
                    "f.release_date, " +
                    "f.description, " +
                    "f.duration, " +
                    "mp.mpa_rating_id, " +
                    "mp.name AS mpa_name " +
                    "FROM FILMS AS f " +
                    "LEFT OUTER JOIN mpa_rating AS mp ON f.mpa_rating_id = mp.mpa_rating_id  " +
                    "LEFT OUTER JOIN film_likes AS fl ON f.film_id = fl.film_id " +
                    "GROUP BY f.film_id, fl.user_id  " +
                    "ORDER BY COUNT(fl.user_id) DESC " +
                    "LIMIT ?";

    public static final String SQL_QUERY_GET_COMMON_FILMS =
            "SELECT fl1.film_id, " +
                    "f.name, " +
                    "f.description, " +
                    "f.release_date, " +
                    "f.duration, " +
                    "mr.mpa_rating_id, " +
                    "mr.name AS mpa_name " +
                    "FROM films AS f " +
                    "LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id) " +
                    "JOIN film_likes AS fl1 ON (fl1.film_id = f.film_id) " +
                    "JOIN film_likes AS fl2 ON (fl2.film_id = f.film_id) " +
                    "JOIN film_likes AS fl3 ON(fl3.film_id = f.film_id) " +
                    "WHERE fl1.user_id = ? AND fl2.user_id = ? " +
                    "GROUP BY fl1.film_id " +
                    "ORDER BY COUNT(fl1.USER_ID) DESC;";

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

    public static final String SQL_QUERY_DELETE_FILM_BY_ID =
            "DELETE FROM FILMS WHERE film_id = ?";

    public static final String SQL_QUERY_DELETE_USER_BY_ID =
            "DELETE FROM USERS WHERE user_id = ?";
}