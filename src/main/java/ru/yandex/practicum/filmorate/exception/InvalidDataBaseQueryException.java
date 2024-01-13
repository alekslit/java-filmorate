package ru.yandex.practicum.filmorate.exception;

import org.springframework.dao.EmptyResultDataAccessException;

public class InvalidDataBaseQueryException extends EmptyResultDataAccessException {
    public static final String INVALID_DATA_BASE_QUERY_MESSAGE = "Не корректный ответ на запрос к базе данных.";
    public static final String USER_INVALID_DATA_BASE_QUERY_ADVICE = "Проверьте корректность введённых данных. " +
            "База данных присылает пустой ответ на ваш запрос. Возможно вы ввели несуществуюший id пользователя.";
    public static final String GENRE_INVALID_DATA_BASE_QUERY_ADVICE = "Проверьте корректность введённых данных. " +
            "База данных присылает пустой ответ на ваш запрос. Возможно вы ввели несуществуюший id жанра.";
    public static final String MPA_INVALID_DATA_BASE_QUERY_ADVICE = "Проверьте корректность введённых данных. " +
            "База данных присылает пустой ответ на ваш запрос. Возможно вы ввели несуществуюший id MPA-рейтинга.";

    // совет пользователю при возникновении исключения:
    private final String adviceToUser;

    public InvalidDataBaseQueryException(String msg, int expectedSize, String adviceToUser) {
        super(msg, expectedSize);
        this.adviceToUser = adviceToUser;
    }

    public String getAdviceToUser() {
        return adviceToUser;
    }
}