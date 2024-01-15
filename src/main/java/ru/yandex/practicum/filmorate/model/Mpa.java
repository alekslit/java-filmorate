package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
public class Mpa {
    private Integer id;

    @Size(max = 40, message = "Слишком длинное название MPA-рейтинга (лимит: {max} символов)")
    private String name;
}