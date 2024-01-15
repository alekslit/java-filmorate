package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.user.UserService;

import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.*;

// класс контроллер для пользователей:
@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public UserController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    // создание User:
    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    // обновление User:
    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    // получение списка всех User:
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // получение User по id:
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        return userService.getUserById(id);
    }

    //удаление пользователя по id
    @DeleteMapping("/{id}")
    public String deleteUserById(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        userService.deleteUserById(id);
        return String.format("фильм с id %d удален", id);
    }

    // добавление User в друзья:
    @PutMapping("/{id}/friends/{friendId}")
    public String addUserToFriends(@PathVariable Long id, @PathVariable Long friendId) {
        checkId(id, PATH_VARIABLE_ID);
        checkId(friendId, PATH_VARIABLE_FRIEND_ID);

        return userService.addUserToFriends(id, friendId);
    }

    // удаление User из друзей:
    @DeleteMapping("/{id}/friends/{friendId}")
    public String removeUserFromFriends(@PathVariable Long id, @PathVariable Long friendId) {
        checkId(id, PATH_VARIABLE_ID);
        checkId(friendId, PATH_VARIABLE_FRIEND_ID);

        return userService.removeUserFromFriends(id, friendId);
    }

    // получаем список друзей User:
    @GetMapping("/{id}/friends")
    public List<User> getAllFriendsList(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        return userService.getAllFriendsList(id);
    }

    // получаем список общих друзей:
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        checkId(id, PATH_VARIABLE_ID);
        checkId(otherId, PATH_VARIABLE_OTHER_ID);

        return userService.getCommonFriends(id, otherId);
    }

    // получаем список рекомендованных фильмов
    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        return userService.getRecommendations(id);
    }

    // получаем ленту событий пользователя:
    @GetMapping("/{id}/feed")
    public List<Event> getEventFeed(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        return eventService.getEventFeed(id);
    }

    // вспомогательный метод для проверки id:
    public void checkId(Long id, String pathVariable) {
        if (id <= 0) {
            log.debug("{}: {} {} = {}", IncorrectPathVariableException.class.getSimpleName(),
                    INCORRECT_PATH_VARIABLE_MESSAGE, pathVariable, id);
            throw new IncorrectPathVariableException(INCORRECT_PATH_VARIABLE_MESSAGE + pathVariable,
                    PATH_VARIABLE_ID_ADVICE);
        }
    }
}