package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortBy {
    DIRECTOR("director"),
    TITLE("title"),
    DIRECTOR_TITLE("director,title"),
    TITLE_DIRECTOR("title,director");

    private final String value;
}