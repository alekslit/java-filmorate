package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Long id);

    String addLikeToFilm(Long id, Long userId);

    String removeLikeFromFilm(Long id, Long userId);

    List<Film> getTopFilmsForLikes(Integer count);
}