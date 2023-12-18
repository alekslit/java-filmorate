package ru.yandex.practicum.filmorate.service.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;

import java.util.List;

@Service
@Qualifier("FilmDbService")
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

}