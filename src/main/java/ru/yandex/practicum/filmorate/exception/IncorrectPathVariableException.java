package ru.yandex.practicum.filmorate.exception;

public class IncorrectPathVariableException extends RuntimeException {
    public static final String INCORRECT_PATH_VARIABLE_MESSAGE = "Некорректно указана переменная пути: ";
    public static final String PATH_VARIABLE_ID = "id";
    public static final String PATH_VARIABLE_FRIEND_ID = "friendId";
    public static final String PATH_VARIABLE_OTHER_ID = "otherId";
    public static final String PATH_VARIABLE_USER_ID = "userId";
    public static final String PATH_VARIABLE_GENRE_ID = "genreId";
    public static final String PATH_VARIABLE_ID_ADVICE = "Проверьте значение переменной пути, "
            + "уникальный идентификатор не может быть пустым, и должен быть положительным, целым числом";

    // совет пользователю при возникновении исключения:
    private final String adviceToUser;

    public IncorrectPathVariableException(String message, String adviceToUser) {
        super(message);
        this.adviceToUser = adviceToUser;
    }

    public String getAdviceToUser() {
        return adviceToUser;
    }
}