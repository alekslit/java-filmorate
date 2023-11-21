package ru.yandex.practicum.filmorate.exception;

public class IncorrectRequestParameterException extends RuntimeException {
    public final static String INCORRECT_REQUEST_PARAM_MESSAGE = "Некорректно указан параметр запроса: ";
    public final static String REQUEST_PARAM_COUNT = "count";
    public final static String REQUEST_PARAMETER_COUNT_ADVICE = "Проверьте значение параметра count, "
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