package ru.yandex.practicum.filmorate.dao.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.event.EventDbStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;

@Repository
@Slf4j
public class UserDbStorage implements UserStorage {
    private static final String ADD_TO_FRIEND_MESSAGE = "Пользователи успешно добавлены в друзья. Их id: ";
    private static final String REMOVE_FROM_FRIEND_MESSAGE = "Пользователи успешно удалены из друзей. Их id: ";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
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
            log.debug("{}: {}{}.", AlreadyExistException.class.getSimpleName(),
                    USER_ALREADY_EXIST_MESSAGE, newUser.getId());
            throw new AlreadyExistException(USER_ALREADY_EXIST_MESSAGE + newUser.getId(), USER_ALREADY_EXIST_ADVICE);
        } else if (newUser.getId() != null) {
            log.debug("{}: {}{}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_NEW_USER_ID_MESSAGE, newUser.getId());
            throw new IllegalIdException(ILLEGAL_NEW_USER_ID_MESSAGE + newUser.getId(), ILLEGAL_NEW_USER_ID_ADVICE);
        }

        log.debug("Добавлен новый пользователь: {} с id = {}.", newUser.getLogin(), newUser.getId());
        return newUser;
    }

    /*---Обновляем данные User в БД---*/
    @Override
    public User updateUser(User user) {
        final User newUser = checkName(user);

        if (getUserById(newUser.getId()) != null) {
            String sqlQuery = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                              "WHERE user_id = ?;";
            jdbcTemplate.update(sqlQuery,
                    newUser.getEmail(),
                    newUser.getLogin(),
                    newUser.getName(),
                    newUser.getBirthday(),
                    newUser.getId());

            log.debug("Обновлена информация о пользователе: {} с id = {}.", newUser.getLogin(), newUser.getId());
        } else if (newUser.getId() == null) {
            addUser(newUser);
            log.debug("Добавлен новый пользователь: {} с id = {}.", newUser.getLogin(), newUser.getId());
        } else {
            log.debug("{}: {}{}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_NEW_USER_ID_MESSAGE, newUser.getId());
            throw new IllegalIdException(ILLEGAL_NEW_USER_ID_MESSAGE + newUser.getId(), ILLEGAL_NEW_USER_ID_ADVICE);
        }

        return newUser;
    }

    /*---Получить список всех User---*/
    @Override
    public List<User> getAllUsers() {
        String sqlQuery =
                "SELECT user_id, " +
                        "email, " +
                        "login, " +
                        "name, " +
                        "birthday " +
                "FROM users;";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    /*---Получить User по id---*/
    @Override
    public User getUserById(Long userId) {
        if (checkIfUserExists(userId)) {
            String sqlQuery =
                    "SELECT user_id, " +
                            "email, " +
                            "login, " +
                            "name, " +
                            "birthday " +
                    "FROM users " +
                    "WHERE user_id = ?;";
            User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, userId);

            return user;
        }
        log.debug("{}: {}{}.", IllegalIdException.class.getSimpleName(), ILLEGAL_USER_ID_MESSAGE, userId);
        throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + userId, ILLEGAL_USER_ID_ADVICE);
    }

    /*---Удалить пользователя по id---*/
    @Override
    public void deleteUserById(Long userId) {
        if (checkIfUserExists(userId)) {
            jdbcTemplate.update("DELETE FROM USERS " +
                                "WHERE user_id = ?;", userId);
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
            // добавляем Event в БД:
            EventDbStorage.addEvent(jdbcTemplate, EventType.FRIEND, EventOperation.ADD, id, friendId);

            return ADD_TO_FRIEND_MESSAGE + id + ", " + friendId;
        } else {
            log.error("{}: {}{} и {}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_OBJECTS_ID_MESSAGE, id, friendId);
            throw new IllegalIdException(ILLEGAL_OBJECTS_ID_MESSAGE + id + " и " + friendId, ILLEGAL_OBJECTS_ID_ADVICE);
        }
    }

    /*---Удаляем User из друзей---*/
    public String removeUserFromFriends(Long id, Long friendId) {
        if (checkIfUserExists(id) && checkIfUserExists(friendId)) {
            String sqlQuery =
                    "DELETE FROM user_friendship " +
                    "WHERE user_id = ? " +
                      "AND friend_id = ?;";
            jdbcTemplate.update(sqlQuery, id, friendId);
            // добавляем Event в БД:
            EventDbStorage.addEvent(jdbcTemplate, EventType.FRIEND, EventOperation.REMOVE, id, friendId);

            return REMOVE_FROM_FRIEND_MESSAGE + id + ", " + friendId;
        } else {
            log.error("{}: {}{} и {}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_OBJECTS_ID_MESSAGE, id, friendId);
            throw new IllegalIdException(ILLEGAL_OBJECTS_ID_MESSAGE + id + " и " + friendId, ILLEGAL_OBJECTS_ID_ADVICE);
        }
    }

    /*---Получить список друзей User---*/
    public List<User> getAllFriendsList(Long id) {
        if (checkIfUserExists(id)) {
            String sqlQuery =
                    "SELECT u.user_id, " +
                            "u.email, " +
                            "u.login, " +
                            "u.name, " +
                            "u.birthday " +
                    "FROM users AS u " +
                    "JOIN user_friendship AS uf ON (u.user_id = uf.friend_id) " +
                    "WHERE uf.user_id = ?;";
            return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
        } else {
            log.error("{}: {}{}.", IllegalIdException.class.getSimpleName(), ILLEGAL_USER_ID_MESSAGE, id);
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + id, ILLEGAL_USER_ID_ADVICE);
        }
    }

    /*---Получить список общих друзей для двух User---*/
    public List<User> getCommonFriends(Long id, Long otherId) {
        if (checkIfUserExists(id) && checkIfUserExists(otherId)) {
            String sqlQuery =
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
            return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id, otherId);
        } else {
            log.error("{}: {}{} и {}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_OBJECTS_ID_MESSAGE, id, otherId);
            throw new IllegalIdException(ILLEGAL_OBJECTS_ID_MESSAGE, ILLEGAL_OBJECTS_ID_ADVICE);
        }
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
            log.debug("Заменяем имя пользователя = {}, на его логин = {}.", user.getName(), user.getLogin());
            return newUser;
        }

        return user;
    }

    private boolean checkIfUserExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM USERS " +
                                                     "WHERE user_id = ?", Integer.class, id);
        if (result == 0) {
            log.error("Пользователя с таким id {} нет", id);
            return false;
        }
        return true;
    }
}