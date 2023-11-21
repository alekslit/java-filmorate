package ru.yandex.practicum.filmorate.exception;

public class IncorrectPathVariableException extends RuntimeException {
    public final static String INCORRECT_PATH_VARIABLE_MESSAGE = "Некорректно указана переменная пути: ";
    public final static String PATH_VARIABLE_ID = "id";
    public final static String PATH_VARIABLE_FRIEND_ID = "friendId";
    public final static String PATH_VARIABLE_OTHER_ID = "otherId";
    public final static String PATH_VARIABLE_USER_ID = "userId";
    public final static String PATH_VARIABLE_ID_ADVICE = "Проверьте значение переменной пути, "
            + "уникальный идентификатор не может быть пустым, и должен быть положительным, целым числом";

    private final String adviceToUser;

    public IncorrectPathVariableException(String message, String adviceToUser) {
        super(message);
        this.adviceToUser = adviceToUser;
    }

    public String getAdviceToUser() {
        return adviceToUser;
    }
}