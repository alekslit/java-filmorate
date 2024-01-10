/*
package ru.yandex.practicum.filmorate.dao.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.INVALID_DATA_BASE_QUERY_MESSAGE;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@JdbcTest
public class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;
    private User user;
    private User userFriend;
    private User commonFriend;

    public void init() {
        userStorage = new UserDbStorage(jdbcTemplate);
        user = User.builder()
                .email("testuser1@tset.com")
                .login("test_user1_login")
                .name("test_user1_name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        userFriend = User.builder()
                .email("testfriend1@tset.com")
                .login("test_friend1_login")
                .name("test_friend1_name")
                .birthday(LocalDate.of(2010, 7, 7))
                .build();
        commonFriend = User.builder()
                .email("testcommonfriend1@tset.com")
                .login("test_common_friend1_login")
                .name("test_common_friend1_name")
                .birthday(LocalDate.of(2003, 3, 3))
                .build();
        // добавляем тестового пользователя:
        userStorage.addUser(user);
    }

    @BeforeEach
    public void setUp() {
        init();
    }

    @Test
    public void testGetUserById() {
        User savedUser = userStorage.getUserById(user.getId());

        assertThat(savedUser)
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    public void testUpdateUser() {
        User updateUser = user.toBuilder()
                .email("updatetestuser1@test.com")
                .login("update_test_user1_login")
                .name("update_test_user1_name")
                .birthday(LocalDate.of(2005, 5,5))
                .build();

        userStorage.updateUser(updateUser);

        assertThat(updateUser)
                .usingRecursiveComparison()
                .isEqualTo(userStorage.getUserById(user.getId()));
    }

    @Test
    public void testGetAllUsers() {
        List<User> userList = userStorage.getAllUsers();

        assertThat(1).isEqualTo(userList.size());
    }

    // проверяем добавление пользователя в друзья и получение списка друзей:
    @Test
    public void testAddUserToFriends() {
        userStorage.addUser(userFriend);

        userStorage.addUserToFriends(user.getId(), userFriend.getId());

        assertThat(1).isEqualTo(userStorage.getAllFriendsList(user.getId()).size());
    }

    // проверяем удаление пользователя из друзей:
    @Test
    public void testRemoveUserFromFriends() {
        userStorage.addUser(userFriend);
        userStorage.addUserToFriends(user.getId(), userFriend.getId());
        assertThat(1).isEqualTo(userStorage.getAllFriendsList(user.getId()).size());

        userStorage.removeUserFromFriends(user.getId(), userFriend.getId());

        assertThat(0).isEqualTo(userStorage.getAllFriendsList(user.getId()).size());
    }

    // проверяем получение списка общих друзей:
    @Test
    public void testGetCommonFriends() {
        userStorage.addUser(userFriend);
        userStorage.addUser(commonFriend);
        userStorage.addUserToFriends(user.getId(), commonFriend.getId());
        userStorage.addUserToFriends(userFriend.getId(), commonFriend.getId());

        List<User> commonFriendList = userStorage.getCommonFriends(user.getId(), userFriend.getId());

        assertThat(commonFriend).isEqualTo(commonFriendList.get(0));
    }

    // Попытка добавить новый User, который уже добавлен:
    @Test
    void shouldAlreadyExistExceptionWhenNewUserWithId() {
        final AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> userStorage.addUser(user));

        assertEquals(USER_ALREADY_EXIST_MESSAGE + user.getId(), exception.getMessage(),
                "Ошибка: User добавлен повторно.");
    }

    // Пробуем обновить User c некорректным id:
    @Test
    void shouldGetInvalidDataBaseQueryExceptionWhenUserIdIs777() {
        user = user.toBuilder()
                .id(777L)
                .build();

        final InvalidDataBaseQueryException exception = assertThrows(
                InvalidDataBaseQueryException.class,
                () -> userStorage.updateUser(user));

        assertEquals(INVALID_DATA_BASE_QUERY_MESSAGE, exception.getMessage(),
                "Обновили User с некорректным id");
    }

    // Проверяем получение User по id, которого не существует:
    @Test
    void shouldGetInvalidDataBaseQueryExceptionWhenGetUserWithIdIs777() {
        final InvalidDataBaseQueryException exception = assertThrows(
                InvalidDataBaseQueryException.class,
                () -> userStorage.getUserById(777L));

        assertEquals(INVALID_DATA_BASE_QUERY_MESSAGE, exception.getMessage(),
                "Ошибка: смогли получить User по несуществующему id.");
    }

    // Проверяем получение User по отрицательному id:
    @Test
    void shouldGetInvalidDataBaseQueryExceptionWhenUSerIdIsNegative() {
        final InvalidDataBaseQueryException exception = assertThrows(
                InvalidDataBaseQueryException.class,
                () -> userStorage.getUserById(-1L));

        assertEquals(INVALID_DATA_BASE_QUERY_MESSAGE,
                exception.getMessage(), "Ошибка: метод работает с отрицательным id.");
    }

    // name = null:
    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        user = user.toBuilder()
                .id(null)
                .name(null)
                .build();

        final User newUser = userStorage.addUser(user);

        assertEquals(user.getLogin(), newUser.getName(), "Ошибка проверки "
                + "имени при User.name = null");
    }
}*/
