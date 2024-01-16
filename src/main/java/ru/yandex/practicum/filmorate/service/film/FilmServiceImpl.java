package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortBy;

import java.util.List;

@Service
public class FilmServiceImpl implements FilmService {
    private final FilmDbStorage filmStorage;

    @Autowired
    public FilmServiceImpl(FilmDbStorage filmStorage) {
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
    public List<Film> searchFilmsByTitleOrDirector(String query, SortBy sortBy) {
        switch (sortBy) {
            case DIRECTOR:
                return filmStorage.searchFilmsByDirector(query);
            case TITLE:
                return filmStorage.searchFilmsByTitle(query);
            case DIRECTOR_TITLE:
            case TITLE_DIRECTOR:
                return filmStorage.searchFilmsByTitleAndDirector(query);
            default:
                throw new IllegalArgumentException("Invalid 'by' parameter. Supported values are 'director', " +
                        "'title', 'director,title' and 'title,director'");
        }
    }

    @Override
    public List<Film> getTopFilmsForLikesWithYearAndGenreFilter(Integer count, Long genreId, Integer year) {
        return filmStorage.getTopFilmsForLikesWithYearAndGenreFilter(count, genreId, year);
    }

    @Override
    public List<Film> getRecommendations(Long id) {
        return filmStorage.getRecommendations(id);
    }
}