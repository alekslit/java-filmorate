package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {
    // идентификатор пользователя:
    private Long id;
    // электронная почта:
    private String email;
    // логин пользователя:
    private String login;
    // имя для отображения:
    private String name;
    // дата рождения:
    private LocalDate birthday;
}