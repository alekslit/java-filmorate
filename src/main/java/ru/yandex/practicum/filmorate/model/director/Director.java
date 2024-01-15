package ru.yandex.practicum.filmorate.model.director;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Director {
    private Integer id;
    @NotBlank
    @Size(max = 255)
    private String name;
}