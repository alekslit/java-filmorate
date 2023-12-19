package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
public class GenreDbService {
    private final GenreDbStorage genreStorage;

    @Autowired
    public GenreDbService(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre getGenreById(Integer genreId) {
        return genreStorage.getGenreById(genreId);
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }
}