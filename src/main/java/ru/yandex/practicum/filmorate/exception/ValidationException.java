package ru.yandex.practicum.filmorate.exception;

// исключение для некорректных объектов Film и User:
public class ValidationException extends RuntimeException {
    public static final String INVALID_FILM_NAME_MESSAGE = "Название фильма не может быть пустым";
    public static final String ILLEGAL_FILM_DESCRIPTION_LENGTH_MESSAGE = "Слишком длинное описание фильма"
            + " (лимит 200 символов)";
    public static final String ILLEGAL_FILM_RELEASE_DATE_MESSAGE = "Дата релиза фильма не может"
            + " быть раньше, чем ";
    public static final String ILLEGAL_FILM_DURATION_MESSAGE = "Продолжительность фильма не может"
            + " быть меньше или равна нулю";
    public static final String INVALID_USER_EMAIL_MESSAGE = "email не может быть пустым"
            + " и должен содержать символ '@'";
    public static final String INVALID_USER_LOGIN_MESSAGE = "Логин не может быть пустым и содержать пробелы";
    public static final String INVALID_USER_BIRTHDAY_MESSAGE = "Дата рождения не может быть позже текущей даты";

    public ValidationException(String message) {
        super(message);
    }
}