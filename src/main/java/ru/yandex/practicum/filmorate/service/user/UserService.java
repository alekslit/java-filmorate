package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    User addUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    User getUserById(Long id);

    void deleteUserById(Long id);

    String addUserToFriends(Long id, Long friendId);

    String removeUserFromFriends(Long id, Long friendId);

    List<User> getAllFriendsList(Long id);

    List<User> getCommonFriends(Long id, Long otherId);
}
