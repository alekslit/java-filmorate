package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
@Slf4j
public class ErrorHandler {
    /*---Обработчики для статуса 400 (Bad request)---*/
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectRequestParameterException(final IncorrectRequestParameterException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(e.getMessage())
                .adviceToUser(e.getAdviceToUser())
                .build();
        log.debug("{}: " + e.getMessage(), IncorrectRequestParameterException.class.getSimpleName());

        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Ошибка валидации данных из запроса.")
                .adviceToUser(e.getFieldError().getDefaultMessage())
                .build();
        log.debug("{}: " + e.getFieldError().getDefaultMessage(),
                MethodArgumentNotValidException.class.getSimpleName());

        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAlreadyExistException(final AlreadyExistException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(e.getMessage())
                .adviceToUser(e.getAdviceToUser())
                .build();
        log.debug("{}: " + e.getMessage(), AlreadyExistException.class.getSimpleName());

        return errorResponse;
    }

    /*---Обработчики для статуса 404 (Not found)---*/
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIllegalIdException(final IllegalIdException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(e.getMessage())
                .adviceToUser(e.getAdviceToUser())
                .build();
        log.debug("{}: " + e.getMessage(), IllegalIdException.class.getSimpleName());

        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncorrectPathVariableException(final IncorrectPathVariableException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(e.getMessage())
                .adviceToUser(e.getAdviceToUser())
                .build();
        log.debug("{}: " + e.getMessage(), IncorrectPathVariableException.class.getSimpleName());

        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleInvalidDataBaseQueryException(final InvalidDataBaseQueryException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(e.getMessage())
                .adviceToUser(e.getAdviceToUser())
                .build();
        log.debug("{}: " + e.getMessage(), InvalidDataBaseQueryException.class.getSimpleName());

        return errorResponse;
    }

    //*---Обработчики для статуса 500 (Internal server error)---*//*
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Произошла непредвиденная ошибка.")
                .adviceToUser("Пожалуйста обратитесь в службу технической поддержки.")
                .build();
        log.debug("{}: " + e.getMessage(), e.getClass().getSimpleName());

        return errorResponse;
    }
}