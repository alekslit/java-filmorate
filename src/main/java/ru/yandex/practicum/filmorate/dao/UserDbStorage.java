package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.INVALID_DATA_BASE_QUERY_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.USER_INVALID_DATA_BASE_QUERY_ADVICE;
import static ru.yandex.practicum.filmorate.query.SqlQuery.*;

@Repository
@Slf4j
public class UserDbStorage implements UserStorage {
    private static final String ADD_TO_FRIEND_MESSAGE = "Пользователи успешно добавлены в друзья. Их id: ";
    private static final String REMOVE_FROM_FRIEND_MESSAGE = "Пользователи успешно удалены из друзей. Их id: ";

    private final JdbcTemplate jdbcTemplate;

    private final FilmDbStorage filmDbStorage;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    /*---Добавляем пользователя в БД---*/
    @Override
    public User addUser(User user) {
        final User newUser = checkName(user);

        if (newUser.getId() == null) {
            SimpleJdbcInsert insertUser = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("users")
                    .usingGeneratedKeyColumns("user_id");
            Long userId = insertUser.executeAndReturnKey(userToMap(newUser)).longValue();
            newUser.setId(userId);
        } else if (getUserById(newUser.getId()) != null) {
            log.debug("{}: " + USER_ALREADY_EXIST_MESSAGE + newUser.getId(),
                    AlreadyExistException.class.getSimpleName());
            throw new AlreadyExistException(USER_ALREADY_EXIST_MESSAGE + newUser.getId(), USER_ALREADY_EXIST_ADVICE);
        } else if (newUser.getId() != null) {
            log.debug("{}: " + ILLEGAL_NEW_USER_ID_MESSAGE + newUser.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_NEW_USER_ID_MESSAGE + newUser.getId(), ILLEGAL_NEW_USER_ID_ADVICE);
        }

        log.debug("Добавлен новый пользователь: " + newUser.getLogin() + ", с id = " + newUser.getId());
        return newUser;
    }

    /*---Обновляем данные User в БД---*/
    @Override
    public User updateUser(User user) {
        final User newUser = checkName(user);

        if (getUserById(newUser.getId()) != null) {
            String sqlQuery = SQL_QUERY_UPDATE_USER;
            jdbcTemplate.update(sqlQuery,
                    newUser.getEmail(),
                    newUser.getLogin(),
                    newUser.getName(),
                    newUser.getBirthday(),
                    newUser.getId());

            log.debug("Обновлена информация о пользователе: " + newUser.getLogin() + ", с id = " + newUser.getId());
        } else if (newUser.getId() == null) {
            addUser(newUser);
            log.debug("Добавлен новый пользователь: " + newUser.getLogin() + ", с id = " + newUser.getId());
        } else {
            log.debug("{}: " + ILLEGAL_USER_ID_MESSAGE + newUser.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + newUser.getId(), ILLEGAL_USER_ID_ADVICE);
        }

        return newUser;
    }

    /*---Получить список всех User---*/
    @Override
    public List<User> getAllUsers() {
        String sqlQuery = SQL_QUERY_GET_ALL_USERS;
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    /*---Получить User по id---*/
    @Override
    public User getUserById(Long userId) {
        try {
            String sqlQuery = SQL_QUERY_GET_USER_BY_ID;
            User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, userId);

            return user;
        } catch (EmptyResultDataAccessException exception) {
            log.debug("{}: " + INVALID_DATA_BASE_QUERY_MESSAGE + " Размер ответа на запрос: "
                    + exception.getExpectedSize(), IllegalIdException.class.getSimpleName());
            throw new InvalidDataBaseQueryException(INVALID_DATA_BASE_QUERY_MESSAGE,
                    exception.getExpectedSize(),
                    USER_INVALID_DATA_BASE_QUERY_ADVICE);
        }
    }

    /*---Удалить пользователя по id---*/
    @Override
    public void deleteUserById(Long userId) {
        if (checkIfUserExists(userId)) {
            jdbcTemplate.update(SQL_QUERY_DELETE_USER_BY_ID, userId);
        } else {
            log.error("Пользователь с id {} еще не добавлен.", userId);
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + userId, ILLEGAL_USER_ID_ADVICE);
        }
    }

    /*---Добавляем User в друзья---*/
    public String addUserToFriends(Long id, Long friendId) {
        if (checkIfUserExists(id) && checkIfUserExists(friendId)) {
            SimpleJdbcInsert insertFriend = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("user_friendship")
                    .usingGeneratedKeyColumns("user_friendship_id");
            insertFriend.execute(userFriendsipToMap(id, friendId));

            return ADD_TO_FRIEND_MESSAGE + id + ", " + friendId;
        } else {
            log.error("Неверно указаны id пользователей для добавления в друзья {}, {}", id, friendId);
            throw new IllegalIdException(ILLEGAL_COMMON_USER_ID_MESSAGE, ILLEGAL_USER_ID_ADVICE);
        }
    }

    /*---Удаляем User из друзей---*/
    public String removeUserFromFriends(Long id, Long friendId) {
        if (checkIfUserExists(id) && checkIfUserExists(friendId)) {
            String sqlQuery = SQL_QUERY_REMOVE_USER_FROM_FRIENDS;
            jdbcTemplate.update(sqlQuery, id, friendId);
            return REMOVE_FROM_FRIEND_MESSAGE + id + ", " + friendId;
        } else {
            log.error("Неверно указаны id пользователей для удаления из друзей {}, {}", id, friendId);
            throw new IllegalIdException(ILLEGAL_COMMON_USER_ID_MESSAGE, ILLEGAL_USER_ID_ADVICE);
        }
    }

    /*---Получить список друзей User---*/

    public List<User> getAllFriendsList(Long id) {
        if (checkIfUserExists(id)) {
            String sqlQuery = SQL_QUERY_GET_ALL_FRIEND_LIST;
            return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
        } else {
            log.error("Пользователь с id {} еще не добавлен.", id);
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + id, ILLEGAL_USER_ID_ADVICE);
        }
    }

    /*---Получить список общих друзей для двух User---*/
    public List<User> getCommonFriends(Long id, Long otherId) {
        if (checkIfUserExists(id) && checkIfUserExists(otherId)) {
            String sqlQuery = SQL_QUERY_GET_COMMON_FRIENDS;
            return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id, otherId);
        } else {
            log.error("Неверно указаны id пользователей для отображения общих друзей {}, {}", id, otherId);
            throw new IllegalIdException(ILLEGAL_COMMON_USER_ID_MESSAGE, ILLEGAL_USER_ID_ADVICE);
        }
    }

    @Override
    public List<Film> getRecommendations(Long id) {
        List<Film> films = new ArrayList<>();
        List<Long> userFilmsId = new ArrayList<>();
        Long anotherUserId = null;
        String userFilmsIdString = "";
        if (checkIfUserExists(id)) {
            try {
                userFilmsId = jdbcTemplate.queryForList(
                        "SELECT film_id " +
                                "FROM film_likes " +
                                "WHERE user_id = ?;", Long.class, id);

                userFilmsIdString = userFilmsId.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",", " ", " "));

                anotherUserId = jdbcTemplate.queryForObject(
                        "SELECT user_id " +
                                "FROM film_likes " +
                                "WHERE NOT user_id = ? " +
                                "AND film_id IN (" + userFilmsIdString + ") " +
                                "GROUP BY user_id " +
                                "ORDER BY COUNT(film_id) DESC " +
                                "LIMIT 1;", Long.class, id);
            } catch (EmptyResultDataAccessException e) {
                return Collections.emptyList();
            }
            films = jdbcTemplate.query(
                    "SELECT fl.film_id, " +
                            "f.name, " +
                            "f.release_date, " +
                            "f.description, " +
                            "f.duration, " +
                            "mp.mpa_rating_id, " +
                            "mp.name AS mpa_name " +
                            "FROM FILM_LIKES AS fl " +
                            "JOIN FILMS AS f ON fl.film_id = f.film_id " +
                            "JOIN mpa_rating AS mp ON f.mpa_rating_id = mp.mpa_rating_id " +
                            "WHERE fl.film_id NOT IN (" + userFilmsIdString + ") " +
                            "AND user_id = ?;", filmDbStorage.getFilmMapper(), anotherUserId);
            filmDbStorage.setGenreForFilms(films);
        }
        return films;
    }

    /*-----Вспомогательные методы-----*/

    private static Map<String, Object> userToMap(User user) {
        return Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday());
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("user_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private static Map<String, Object> userFriendsipToMap(Long id, Long friendId) {
        return Map.of(
                "user_id", id,
                "friend_id", friendId);
    }

    @Override
    // проверяем имя пользователя, если пустое, то name = login:
    public User checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            final User newUser = user.toBuilder()
                    .name(user.getLogin())
                    .build();
            log.debug("User.name = " + user.getName() + ", заменяем User.name на User.login = " + user.getLogin());
            return newUser;
        }

        return user;
    }

    private boolean checkIfUserExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS WHERE user_id = ?",
                Integer.class, id);
        if (result == 0) {
            log.error("Пользователя с таким id {} нет", id);
            return false;
        }
        return true;
    }
}