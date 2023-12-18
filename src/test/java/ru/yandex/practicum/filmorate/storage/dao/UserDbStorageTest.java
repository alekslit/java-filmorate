package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
                .birthday(LocalDate.of(2005, 05,05))
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

    @Test
    public void testFriendshipFunctional() {
        // проверим добавление и получение списка друзей:
        userStorage.addUser(userFriend);

        userStorage.addUserToFriends(user.getId(), userFriend.getId());

        assertThat(1).isEqualTo(userStorage.getAllFriendsList(user.getId()).size());

        // проверим получение списка общих друзей:
        userStorage.addUser(commonFriend);
        userStorage.addUserToFriends(user.getId(), commonFriend.getId());
        userStorage.addUserToFriends(userFriend.getId(), commonFriend.getId());

        List<User> commonFriendList = userStorage.getCommonFriends(user.getId(), userFriend.getId());

        assertThat(1).isEqualTo(commonFriendList.size());

        //проверим удаление из друзей:
        userStorage.removeUserFromFriends(user.getId(), userFriend.getId());

        assertThat(1).isEqualTo(userStorage.getAllFriendsList(user.getId()).size());
    }
}