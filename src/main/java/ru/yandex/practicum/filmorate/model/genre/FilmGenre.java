package ru.yandex.practicum.filmorate.model.genre;

import lombok.Data;

@Data
public class FilmGenre {
    private Long filmId;
    private Genre genre;

    public FilmGenre(Long filmId, Genre genre) {
        this.filmId = filmId;
        this.genre = genre;
    }
}