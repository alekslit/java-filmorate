package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private static final String ADD_TO_FRIEND_MESSAGE = "Пользователи успешно добавлены в друзья. Их id: ";
    private static final String REMOVE_FROM_FRIEND_MESSAGE = "Пользователи успешно удалены из друзей. Их id: ";

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public String addUserToFriends(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        return ADD_TO_FRIEND_MESSAGE + id + ", " + friendId;
    }

    public String removeUserFromFriends(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        return REMOVE_FROM_FRIEND_MESSAGE + id + ", " + friendId;
    }

    public List<User> getAllFriendsList(Long id) {
        User userWithFriend = userStorage.getUserById(id);

        if (userWithFriend.getFriends() == null) {
            log.debug(String.format("Список друзей пуст (User.friends = null). User.id = %d.", id));
            return new ArrayList<>();
        }

        List<User> userFriend = userStorage.getAllUsers().stream()
                .filter(user -> userWithFriend.getFriends().contains(user.getId()))
                .collect(Collectors.toList());

        return userFriend;
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> user = getAllFriendsList(id);
        List<User> otherUser = getAllFriendsList(otherId);

        List<User> commonFriends = user.stream()
                .filter(otherUser::contains)
                .collect(Collectors.toList());

        return commonFriends;
    }
}