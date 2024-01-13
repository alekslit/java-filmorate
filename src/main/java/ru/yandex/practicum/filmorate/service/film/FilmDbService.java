package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
public class FilmDbService implements FilmService {
    private final FilmDbStorage filmStorage;

    @Autowired
    public FilmDbService(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
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
    public void deleteFilmById(Long id) {
        filmStorage.deleteFilmById(id);
    }

    @Override
    public String addLikeToFilm(Long id, Long userId) {
        return filmStorage.addLikeToFilm(id, userId);
    }

    @Override
    public String removeLikeFromFilm(Long id, Long userId) {
        return filmStorage.removeLikeFromFilm(id, userId);
    }

    @Override
    public List<Film> getTopFilmsForLikes(Integer count) {
        return filmStorage.getTopFilmsForLikes(count);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikesOrYear(Long directorId, boolean sortByLikes) {
        return filmStorage.getFilmsByDirectorSortedByLikesOrYear(directorId, sortByLikes);
    }

    @Override
    public List<Film> searchFilmsByDirector(String query) {
        return filmStorage.searchFilmsByDirector(query);
    }

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        return filmStorage.searchFilmsByTitle(query);
    }

    @Override
    public List<Film> searchFilmsByTitleAndDirector(String query) {
        return filmStorage.searchFilmsByTitleAndDirector(query);
    }

    @Override
    public List<Film> getTopFilmsForLikesWithYearAndGenreFilter(Integer count, Long genreId, Integer year) {
        return filmStorage.getTopFilmsForLikesWithYearAndGenreFilter(count, genreId, year);
    }
}