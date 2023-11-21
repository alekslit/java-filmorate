package ru.yandex.practicum.filmorate.exception;

public class IllegalIdException extends RuntimeException {
    /*---для обновления и получения уже существующих объектов---*/
    public static final String ILLEGAL_USER_ID_MESSAGE = "Пользователя с таким id не существует. id = ";
    public static final String ILLEGAL_FILM_ID_MESSAGE = "Фильма с таким id не существует. id = ";
    public static final String ILLEGAL_FILM_ID_ADVICE = "Проверьте значение переданного id фильма.";
    public static final String ILLEGAL_USER_ID_ADVICE = "Проверьте значение переданного id пользователя.";

    /*---для попыток добавить новый объект---*/
    public static final String ILLEGAL_NEW_FILM_ID_MESSAGE = "Фильм содержит id. id = ";
    public static final String ILLEGAL_NEW_USER_ID_MESSAGE = "Пользователь содержит id. id = ";
    public static final String ILLEGAL_NEW_FILM_ID_ADVICE = "Проверьте id фильма. Новый фильм не должен содержать id."
            + " Программа генерирует id для нового фильма автоматически.";
    public static final String ILLEGAL_NEW_USER_ID_ADVICE = "Проверьте id пользователя. Новый пользователь"
            + " не должен содержать id. Программа генерирует id для нового пользователя автоматически.";

    private final String adviceToUser;

    public IllegalIdException(String message, String adviceToUser) {
        super(message);
        this.adviceToUser = adviceToUser;
    }

    public String getAdviceToUser() {
        return adviceToUser;
    }
}