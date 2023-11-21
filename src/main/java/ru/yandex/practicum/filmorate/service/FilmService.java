package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public String addLikeToFilm(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);
        film.getLikes().add(user.getId());

        return String.format("Пользователь с id: %d, поставил лайк фильму: %s.", user.getId(), film.getName());
    }

    public String removeLikeFromFilm(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);
        film.getLikes().remove(user.getId());

        return String.format("Пользователь с id: %d, удалил свой лайк фильму: %s.", user.getId(), film.getName());
    }

    public List<Film> getTopFilmsForLikes(Integer count) {
        List<Film> topFilmsForLikes = filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparing(Film::getAmountOfLikes).reversed())
                .limit(count)
                .collect(Collectors.toList());

        return topFilmsForLikes;
    }

    public FilmStorage getFilmStorage() {
        return filmStorage;
    }
}
