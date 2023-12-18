package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

// класс для объекта возвращаемого пользователю при возникновении ошибки / исключения:
@Data
@Builder
public class ErrorResponse {
    private String error;
    private String adviceToUser;
}