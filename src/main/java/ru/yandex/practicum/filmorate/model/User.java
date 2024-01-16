package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.NotContainsSymbol;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {
    // идентификатор пользователя:
    private Long id;

    // электронная почта:
    @NotNull(message = "Адрес электронной почты не может быть пустым")
    @Email(regexp = "([A-Za-z0-9]{1,}[\\\\-]{0,1}[A-Za-z0-9]{1,}[\\\\.]{0,1}[A-Za-z0-9]{1,})+@"
            + "([A-Za-z0-9]{1,}[\\\\-]{0,1}[A-Za-z0-9]{1,}[\\\\.]{0,1}[A-Za-z0-9]{1,})+[\\\\.]{1}[a-z]{2,10}",
            message = "Некорректный адресс электронной почты: ${validatedValue}")
    @Size(max = 100, message = "Слишком длинный email (лимит: {max} символов)")
    private String email;

    // логин пользователя:
    @NotBlank(message = "Логин пользователя не может быть пустым")
    @NotContainsSymbol(message = "Логин пользователя не может содержать символ: '{value}'")
    @Size(max = 40, message = "Слишком длинный логин пользователя (лимит: {max} символов)")
    private String login;

    // имя для отображения:
    @Size(max = 40, message = "Слишком длинное имя пользователя (лимит: {max} символов)")
    private String name;

    // дата рождения:
    @PastOrPresent(message = "Дата рождения пользователя не может быть позже сегодняшней даты")
    private LocalDate birthday;
}