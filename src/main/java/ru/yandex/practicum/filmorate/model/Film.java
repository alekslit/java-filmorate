package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Film {
    // идентификатор фильма:
    private Long id;
    // название:
    private String name;
    // описание:
    private String description;
    // дата релиза:
    private LocalDate releaseDate;
    // продолжительность в минутах:
    private Integer duration;
}