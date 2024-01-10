package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    User checkName(User user);

    User getUserById(Long userId);

    void deleteUserById(Long userId);

    List<Film> getRecommendations(Long id);

}