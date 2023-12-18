package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("InMemoryFilmService")
public class InMemoryFilmService implements FilmService{
    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public InMemoryFilmService( InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    @Override
    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @Override
    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    @Override
    public String addLikeToFilm(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);
        film.getLikes().add(user.getId());

        return String.format("Пользователь с id: %d, поставил лайк фильму: %s.", user.getId(), film.getName());
    }

    @Override
    public String removeLikeFromFilm(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);
        film.getLikes().remove(user.getId());

        return String.format("Пользователь с id: %d, удалил свой лайк фильму: %s.", user.getId(), film.getName());
    }

    @Override
    public List<Film> getTopFilmsForLikes(Integer count) {
        List<Film> topFilmsForLikes = filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparing(Film::getAmountOfLikes).reversed())
                .limit(count)
                .collect(Collectors.toList());

        return topFilmsForLikes;
    }
}