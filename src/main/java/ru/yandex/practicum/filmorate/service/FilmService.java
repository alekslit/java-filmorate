package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Long id);

    void deleteFilmById(Long id);

    String addLikeToFilm(Long id, Long userId);

    String removeLikeFromFilm(Long id, Long userId);

    List<Film> getTopFilmsForLikes(Integer count);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getTopFilmsForLikesWithYearAndGenreFilter(Integer count, Long genreId, Integer year);
}