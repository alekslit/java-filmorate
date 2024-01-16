package ru.yandex.practicum.filmorate.exception;

// общий родительский класс для исключений приложения:
public class CustomException extends RuntimeException {
    // совет пользователю при возникновении исключения:
    private final String adviceToUser;

    public CustomException(String message, String adviceToUser) {
        super(message);
        this.adviceToUser = adviceToUser;
    }

    public String getAdviceToUser() {
        return adviceToUser;
    }
}