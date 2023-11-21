package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long currentUserIdNumber = 0L;

    @Override
    public User addUser(User user) {
        final User newUser = checkName(user);

        if (users.containsKey(newUser.getId())) {
            log.debug("{}: " + USER_ALREADY_EXIST_MESSAGE + newUser.getId(),
                    AlreadyExistException.class.getSimpleName());
            throw new AlreadyExistException(USER_ALREADY_EXIST_MESSAGE + newUser.getId(), USER_ALREADY_EXIST_ADVICE);
        }
        if (user.getId() != null) {
            log.debug("{}: " + ILLEGAL_NEW_USER_ID_MESSAGE + newUser.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_NEW_USER_ID_MESSAGE + newUser.getId(), ILLEGAL_NEW_USER_ID_ADVICE);
        }
        newUser.setId(generateId());
        log.debug("Добавлен новый пользователь: " + newUser.getLogin() + ", с id = " + newUser.getId());
        users.put(newUser.getId(), newUser);

        return newUser;
    }

    @Override
    public User updateUser(User user) {
        final User newUser = checkName(user);

        if (users.containsKey(newUser.getId())) {
            users.put(newUser.getId(), newUser);
            log.debug("Обновлена информация о пользователе: " + newUser.getLogin() + ", с id = " + newUser.getId());
        } else if (newUser.getId() == null) {
            newUser.setId(generateId());
            users.put(newUser.getId(), newUser);
            log.debug("Добавлен новый пользователь: " + newUser.getLogin() + ", с id = " + newUser.getId());
        } else {
            log.debug("{}: " + ILLEGAL_USER_ID_MESSAGE + newUser.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + newUser.getId(), ILLEGAL_USER_ID_ADVICE);
        }

        return newUser;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    // генератор id:
    public Long generateId() {
        return ++currentUserIdNumber;
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

    @Override
    public User getUserById(Long userId) {
        if (users.get(userId) == null) {
            log.debug("{}: " + ILLEGAL_USER_ID_MESSAGE + userId,
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + userId, ILLEGAL_USER_ID_ADVICE);
        }

        return users.get(userId);
    }
}