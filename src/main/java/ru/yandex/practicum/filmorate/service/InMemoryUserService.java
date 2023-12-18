package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("InMemoryUserService")
public class InMemoryUserService implements UserService{
    private static final String ADD_TO_FRIEND_MESSAGE = "Пользователи успешно добавлены в друзья. Их id: ";
    private static final String REMOVE_FROM_FRIEND_MESSAGE = "Пользователи успешно удалены из друзей. Их id: ";

    private final InMemoryUserStorage userStorage;

    @Autowired
    public InMemoryUserService(InMemoryUserStorage userStorage) {
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
        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        return ADD_TO_FRIEND_MESSAGE + id + ", " + friendId;
    }

    @Override
    public String removeUserFromFriends(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        return REMOVE_FROM_FRIEND_MESSAGE + id + ", " + friendId;
    }

    @Override
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

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> user = getAllFriendsList(id);
        List<User> otherUser = getAllFriendsList(otherId);

        List<User> commonFriends = user.stream()
                .filter(otherUser::contains)
                .collect(Collectors.toList());

        return commonFriends;
    }
}