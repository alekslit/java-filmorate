package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
public class Review {
    private Long reviewId;
    @NotBlank
    private String content;
    // @JsonProperty("isPositive")
    // objectMapper не кладёт булеан по умолчанию, так как его геттер не имеет префикс get
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    private Long useful;
}