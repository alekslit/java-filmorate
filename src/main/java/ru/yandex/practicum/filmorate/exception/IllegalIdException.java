package ru.yandex.practicum.filmorate.exception;

public class IllegalIdException extends RuntimeException {
    public static final String ILLEGAL_USER_ID_MESSAGE = "Некорректный id пользователя: ";
    public static final String ILLEGAL_FILM_ID_MESSAGE = "Некорректный id фильма: ";

    public IllegalIdException(String message) {
        super(message);
    }
}
