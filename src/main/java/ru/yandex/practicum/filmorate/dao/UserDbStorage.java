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
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

    /*---Добавляем User в друзья---*/
    public String addUserToFriends(Long id, Long friendId) {
        SimpleJdbcInsert insertFriend = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_friendship")
                .usingGeneratedKeyColumns("user_friendship_id");
        insertFriend.execute(userFriendsipToMap(id, friendId));

        return ADD_TO_FRIEND_MESSAGE + id + ", " + friendId;
    }

    /*---Удаляем User из друзей---*/
    public String removeUserFromFriends(Long id, Long friendId) {
        String sqlQuery = SQL_QUERY_REMOVE_USER_FROM_FRIENDS;
        jdbcTemplate.update(sqlQuery, id, friendId);

        return REMOVE_FROM_FRIEND_MESSAGE + id + ", " + friendId;
    }

    /*---Получить список друзей User---*/
    public List<User> getAllFriendsList(Long id) {
        String sqlQuery = SQL_QUERY_GET_ALL_FRIEND_LIST;
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
    }

    /*---Получить список общих друзей для двух User---*/
    public List<User> getCommonFriends(Long id, Long otherId) {
        String sqlQuery = SQL_QUERY_GET_COMMON_FRIENDS;
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id, otherId);
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
}