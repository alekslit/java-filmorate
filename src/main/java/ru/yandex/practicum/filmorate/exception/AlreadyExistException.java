package ru.yandex.practicum.filmorate.exception;

public class AlreadyExistException extends CustomException {
    public static final String USER_ALREADY_EXIST_MESSAGE = "Невозможно добавить нового пользователя, "
            + "пользователь с таким id уже существует. Id пользователя: ";
    public static final String FILM_ALREADY_EXIST_MESSAGE = "Невозможно добавить новый фильм, "
            + "фильм с таким id уже существует. Id фильма: ";
    public static final String FILM_ALREADY_EXIST_ADVICE = "Проверьте id фильма. Новый фильм не должен содержать id."
            + " Программа генерирует id для нового фильма автоматически.";
    public static final String USER_ALREADY_EXIST_ADVICE = "Проверьте id пользователя. Новый пользователь"
            + " не должен содержать id. Программа генерирует id для нового пользователя автоматически.";

    public AlreadyExistException(String message, String adviceToUser) {
        super(message, adviceToUser);
    }
}