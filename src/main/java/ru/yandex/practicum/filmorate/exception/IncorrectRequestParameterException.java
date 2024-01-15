package ru.yandex.practicum.filmorate.exception;

public class IncorrectRequestParameterException extends CustomException {

    public IncorrectRequestParameterException(String message, String adviceToUser) {
        super(message, adviceToUser);
    }
}