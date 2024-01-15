package ru.yandex.practicum.filmorate.model.genre;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
public class Genre {
    private Integer id;
    @Size(max = 40, message = "Слишком длинное название жанра фильма (лимит: {max} символов)")
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
}