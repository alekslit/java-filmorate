package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_USER_ID_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.ValidationException.*;

public class UserControllerTests {
    private UserController userController;
    private User user;

    private void init() {
        userController = new UserController();
        user = User.builder()
                .email("perelman@yandex.ru")
                .login("perelman")
                .name("genius")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();
        userController.addUser(user);
    }

    @BeforeEach
    void beforeEach() {
        init();
    }

    // Добавление нового User + получение списка всех User:
    @Test
    void shouldAddNewUserAndGetAllUsers() {
        final List<User> usersList = userController.getAllUsers();

        assertNotNull(usersList, "Список пуст, User не добавлен (shouldAddNewUserAndGetAllUsers)");
    }

    // Попытка добавить новый User, который уже добавлен:
    @Test
    void shouldAlreadyExistExceptionWhenNewUserWithId() {
        user = user.toBuilder()
                .id(1L)
                .build();

        final AlreadyExistException exception =assertThrows(
                AlreadyExistException.class,
                () -> userController.addUser(user));

        assertEquals(USER_ALREADY_EXIST_MESSAGE + user.getLogin(), exception.getMessage(), "Ошибка: "
                + "User добавлен повторно (shouldAlreadyExistExceptionWhenNewUserWithId)");
    }

    // Пробуем обновить User с существующим id:
    @Test
    void shouldUpdateUser() {
        user = user.toBuilder()
                .name("pereL")
                .build();

        userController.updateUser(user);

        assertEquals(user.getName(), userController.getUserById(user.getId()).getName(), "Не "
                + "получилось обновить User (shouldUpdateUser)");
    }

    // Пробуем обновить User c некорректным id:
    @Test
    void shouldGetIllegalExceptionWhenUserIdIs777() {
        user = User.builder()
                .id(777L)
                .email("obman@yandex.ru")
                .login("false")
                .name("smeshnoi")
                .birthday(LocalDate.of(1901, 1, 1))
                .build();

        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> userController.updateUser(user));

        assertEquals(ILLEGAL_USER_ID_MESSAGE + user.getId(), exception.getMessage(), "Обновили "
                + "User с некорректным id (shouldGetIllegalExceptionWhenUserIdIs777)");
    }

    /*---Проверяем работу валидации объектов User---*/
    @Test
    void shouldValidationExceptionWhenUserEmailWithoutAt() {
        user = user.toBuilder()
                .email("withoutsobakayandex.ru")
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.validateUser(user));

        assertEquals(INVALID_USER_EMAIL_MESSAGE, exception.getMessage(), "Некорректный"
                + " объект User прошёл валидацию (shouldValidationExceptionWhenUserEmailWithoutAt)");
    }

    @Test
    void shouldValidationExceptionWhenUserLoginWithBlankSpace() {
        user = user.toBuilder()
                .login("p e r e l m a n")
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.validateUser(user));

        assertEquals(INVALID_USER_LOGIN_MESSAGE, exception.getMessage(), "Некорректный"
                + " объект User прошёл валидацию (shouldValidationExceptionWhenUserLoginWithBlankSpace)");
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        user = user.toBuilder()
                .name(null)
                .build();

        final User newUser = userController.validateUser(user);

        assertEquals(user.getLogin(), newUser.getName(), "Некорректный объект User прошёл валидацию "
                        + "(shouldUseLoginAsNameWhenNameIsNull)");
    }

    @Test
    void shouldValidationExceptionWhenUserBirthdayIsFutureDay() {
        final LocalDate futureDay = LocalDate.now().plusDays(1);
        user = user.toBuilder()
                .birthday(futureDay)
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.validateUser(user));

        assertEquals(INVALID_USER_BIRTHDAY_MESSAGE, exception.getMessage(), "Некорректный объект User прошёл "
                + "валидацию (shouldValidationExceptionWhenUserBirthdayIsFutureDay)");
    }
}