package ru.yandex.practicum.filmorate.model.genre;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Genre {
    private Integer id;
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
}