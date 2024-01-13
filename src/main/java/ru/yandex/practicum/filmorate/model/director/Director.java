package ru.yandex.practicum.filmorate.model.director;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Director {
    Integer id;
    @NotBlank
    String name;
}