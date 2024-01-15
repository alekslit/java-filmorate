package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Long filmId);

    void deleteFilmById(Long filmId);

    List<Film> getFilmsByDirectorSortedByLikesOrYear(Long directorId, boolean sortByLikes);

    List<Film> searchFilmsByDirector(String query);

    List<Film> searchFilmsByTitle(String query);

    List<Film> searchFilmsByTitleAndDirector(String query);

    List<Film> getRecommendations(Long id);
}