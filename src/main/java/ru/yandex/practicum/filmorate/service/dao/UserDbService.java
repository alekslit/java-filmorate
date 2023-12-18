package ru.yandex.practicum.filmorate.service.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.util.List;

@Service
@Qualifier("UserDbService")
public class UserDbService implements UserService {
    private static final String REMOVE_FROM_FRIEND_MESSAGE = "Пользователи успешно удалены из друзей. Их id: ";

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