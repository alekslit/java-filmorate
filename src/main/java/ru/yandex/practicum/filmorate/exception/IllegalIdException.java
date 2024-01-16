package ru.yandex.practicum.filmorate.exception;

public class IllegalIdException extends CustomException {
    /*---для обновления и получения уже существующих объектов---*/
    public static final String ILLEGAL_USER_ID_MESSAGE = "Пользователя с таким id не существует. id = ";
    public static final String ILLEGAL_REVIEW_ID_MESSAGE = "Отзыва с таким id не существует. id = ";
    public static final String ILLEGAL_FILM_ID_MESSAGE = "Фильма с таким id не существует. id = ";
    public static final String ILLEGAL_GENRE_ID_MESSAGE = "Жанра с таким id не существует. id = ";
    public static final String ILLEGAL_MPA_ID_MESSAGE = "MPA-рейтинг с таким id не существует. id = ";
    public static final String ILLEGAL_DIRECTOR_ID_MESSAGE = "Режиссёра с таким id не существует. id = ";
    public static final String ILLEGAL_OBJECTS_ID_MESSAGE = "Переданные id имеют некорректные значения. id = ";
    public static final String ILLEGAL_REVIEW_ID_ADVICE = "Проверьте значение переданного id отзыва.";
    public static final String ILLEGAL_FILM_ID_ADVICE = "Проверьте значение переданного id фильма.";
    public static final String ILLEGAL_GENRE_ID_ADVICE = "Проверьте значение переданного id жанра.";
    public static final String ILLEGAL_MPA_ID_ADVICE = "Проверьте значение переданного id MPA-рейтинга.";
    public static final String ILLEGAL_USER_ID_ADVICE = "Проверьте значение переданного id пользователя.";
    public static final String ILLEGAL_DIRECTOR_ID_ADVICE = "Проверьте значение переданного id режиссёра.";
    public static final String ILLEGAL_OBJECTS_ID_ADVICE = "Проверьте значение переданных id.";

    /*---для попыток добавить новый объект---*/
    public static final String ILLEGAL_NEW_REVIEW_ID_MESSAGE = "Отзыв уже содержит id, либо фильм или пользователь " +
            "имеют некорректный id.";
    public static final String ILLEGAL_NEW_FILM_ID_MESSAGE = "Фильм содержит id. id = ";
    public static final String ILLEGAL_NEW_USER_ID_MESSAGE = "Пользователь содержит id. id = ";
    public static final String ILLEGAL_NEW_FILM_ID_ADVICE = "Проверьте id фильма. Новый фильм не должен содержать id."
            + " Программа генерирует id для нового фильма автоматически.";
    public static final String ILLEGAL_NEW_USER_ID_ADVICE = "Проверьте id пользователя. Новый пользователь"
            + " не должен содержать id. Программа генерирует id для нового пользователя автоматически.";
    public static final String ILLEGAL_NEW_REVIEW_ID_ADVICE = "Проверьте id пользователя, фильма и отзыва. Новый " +
            "отзыв не должен содержать id. Программа генерирует id для нового отзыва автоматически. Пользователь " +
            "и фильм должны содержать существующие id.";

    public IllegalIdException(String message, String adviceToUser) {
        super(message, adviceToUser);
    }
}