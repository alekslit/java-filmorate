package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
public class UserDbService implements UserService {
    private final UserDbStorage userStorage;

    @Autowired
    public UserDbService(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    @Override
    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    @Override
    public void deleteUserById(Long id) {
        userStorage.deleteUserById(id);
    }

    @Override
    public String addUserToFriends(Long id, Long friendId) {
        return userStorage.addUserToFriends(id, friendId);
    }

    @Override
    public String removeUserFromFriends(Long id, Long friendId) {
        return userStorage.removeUserFromFriends(id, friendId);
    }

    @Override
    public List<User> getAllFriendsList(Long id) {
        return userStorage.getAllFriendsList(id);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }
}