package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_USER_ID_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.ValidationException.*;

// класс контроллер для пользователей:
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    // храним данные в памяти приложения:
    private final Map<Long, User> users = new HashMap<>();
    private Long currentUserIdNumber = 0L;

    // создание пользователя:
    @PostMapping
    public User addUser(@RequestBody User user) {
        final User newUser = validateUser(user);
        if (users.containsKey(newUser.getId())) {
            log.debug("{}: " + USER_ALREADY_EXIST_MESSAGE + newUser.getLogin(),
                    AlreadyExistException.class.getSimpleName());
            throw new AlreadyExistException(USER_ALREADY_EXIST_MESSAGE + newUser.getLogin());
        }
        if (user.getId() != null) {
            log.debug("{}: " + ILLEGAL_USER_ID_MESSAGE + newUser.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + newUser.getId());
        }
        newUser.setId(generateId());
        log.debug("Добавлен новый пользователь: " + newUser.getLogin() + ", с id = " + newUser.getId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    // обновление пользователя:
    @PutMapping
    public User updateUser(@RequestBody User user) {
        final User newUser = validateUser(user);
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
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + newUser.getId());
        }

        return newUser;
    }

    // получение списка всех пользователей:
    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // метод для проверки полей объекта User:
    public User validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.debug("{}: " + INVALID_USER_EMAIL_MESSAGE, ValidationException.class.getSimpleName());
            throw new ValidationException(INVALID_USER_EMAIL_MESSAGE);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.debug("{}: " + INVALID_USER_LOGIN_MESSAGE, ValidationException.class.getSimpleName());
            throw new ValidationException(INVALID_USER_LOGIN_MESSAGE);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("{}: " + INVALID_USER_BIRTHDAY_MESSAGE, ValidationException.class.getSimpleName());
            throw new ValidationException(INVALID_USER_BIRTHDAY_MESSAGE);
        }
        // если нет имени, то используем логин как имя:
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            return user;
        }

        return user;
    }

    // генератор id:
    private Long generateId() {
        return ++currentUserIdNumber;
    }

    // вспомогательный метод (получить фильм по id):
    public User getUserById(Long userId) {
        if (users.get(userId) == null) {
            log.debug("Нет User с таким id = " + userId);
            return null;
        }

        return users.get(userId);
    }
}