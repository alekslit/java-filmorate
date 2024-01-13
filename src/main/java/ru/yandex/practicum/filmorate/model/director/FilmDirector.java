package ru.yandex.practicum.filmorate.model.director;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilmDirector {
    private Long filmId;
    private int directorId;
    private String director;
}