package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.user.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;

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

    @Override
    public List<Film> getRecommendations(Long id) {
        return userStorage.getRecommendations(id);
    }

    @Override
    public List<Event> getEventFeed(Long userId) {
        return userStorage.getEventFeed(userId);
    }
}