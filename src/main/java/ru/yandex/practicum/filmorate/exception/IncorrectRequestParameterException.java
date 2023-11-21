package ru.yandex.practicum.filmorate.exception;

public class IncorrectRequestParameterException extends RuntimeException {
    public static final String INCORRECT_REQUEST_PARAM_MESSAGE = "Некорректно указан параметр запроса: ";
    public static final String REQUEST_PARAM_COUNT = "count";
    public static final String REQUEST_PARAMETER_COUNT_ADVICE = "Проверьте значение параметра count, "
            + "оно должно быть положительным, целым числом";

    // совет пользователю при возникновении исключения:
    private final String adviceToUser;

    public IncorrectRequestParameterException(String message, String adviceToUser) {
        super(message);
        this.adviceToUser = adviceToUser;
    }

    public String getAdviceToUser() {
        return adviceToUser;
    }
}