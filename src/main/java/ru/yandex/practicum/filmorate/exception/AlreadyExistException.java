package ru.yandex.practicum.filmorate.exception;

public class AlreadyExistException extends RuntimeException {
    public static final String USER_ALREADY_EXIST_MESSAGE = "Невозможно добавить нового пользователя, "
            + "такой пользователь уже существует: ";
    public static final String FILM_ALREADY_EXIST_MESSAGE = "Невозможно добавить новый фильм, "
            + "такой фильм уже существует: ";

    public AlreadyExistException(String message) {
        super(message);
    }
}
