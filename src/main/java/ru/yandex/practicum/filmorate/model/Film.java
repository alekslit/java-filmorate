package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.FutureOrPresentSelectDate;
import ru.yandex.practicum.filmorate.model.director.Director;
import ru.yandex.practicum.filmorate.model.genre.Genre;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Film {
    // идентификатор фильма:
    private Long id;

    // название:
    @Size(max = 40, message = "Слишком длинное название фильма (лимит: {max} символов)")
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    // описание:
    @Size(max = 200, message = "Слишком длинное описание фильма (лимит: {max} символов)")
    private String description;

    // дата релиза:
    @FutureOrPresentSelectDate(message = "Дата релиза фильма должна быть позже или равняться {value}")
    private LocalDate releaseDate;

    // продолжительность в минутах:
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    @NotNull(message = "Продолжительность фильма не может быть пустой.")
    private Integer duration;

    // MPA-рейтинг фильма:
    private Mpa mpa;

    // жанры фильма:
    private Set<Genre> genres;

    // режиссёры фильма:
    private Set<Director> directors;
}