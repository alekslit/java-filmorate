package ru.yandex.practicum.filmorate.model.genre;

import lombok.Data;

@Data
public class FilmGenre {
    private Long filmId;
    private int genreId;
    private String genre;

    public FilmGenre(long filmId, int genreId, String genre) {
        this.filmId = filmId;
        this.genreId = genreId;
        this.genre = genre;
    }
}