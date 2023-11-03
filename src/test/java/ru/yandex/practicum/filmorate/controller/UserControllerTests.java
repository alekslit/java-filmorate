package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.jsontypeadapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.USER_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_USER_ID_MESSAGE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerTests {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private static final String URL = "http://localhost:8080";

    private UserController userController;
    private User user;
    private User userByValidationCheck;
    private HttpClient httpClient;

    private void init() {
        userController = new UserController();
        httpClient = HttpClient.newHttpClient();
        user = User.builder()
                .email("perelman@yandex.ru")
                .login("perelman")
                .name("genius")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();
        userByValidationCheck = User.builder()
                .email("obman@yandex.ru")
                .login("negodnik")
                .name("Smeshnoi")
                .birthday(LocalDate.of(1901, 1, 1))
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

        final AlreadyExistException exception = assertThrows(
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
        userByValidationCheck = userByValidationCheck.toBuilder()
                .id(777L)
                .build();

        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> userController.updateUser(userByValidationCheck));

        assertEquals(ILLEGAL_USER_ID_MESSAGE + userByValidationCheck.getId(), exception.getMessage(), "Обновили "
                + "User с некорректным id (shouldGetIllegalExceptionWhenUserIdIs777)");
    }

    /*---Тесты валидации объекта User---*/
    // email без @:
    @Test
    void shouldNot200StatusCodeWhenUserEmailWithoutAt() {
        final URI uri = URI.create(URL + "/users");
        Integer responseStatusCode = 200;
        userByValidationCheck = userByValidationCheck.toBuilder()
                .email("withoutsobakayandex.ru")
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userByValidationCheck, User.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при User.email без '@'");
    }

    // login с пробелами:
    @Test
    void shouldNot200StatusCodeWhenUserLoginWithBlankSpace() {
        final URI uri = URI.create(URL + "/users");
        Integer responseStatusCode = 200;
        userByValidationCheck = userByValidationCheck.toBuilder()
                .login("n e g o d n i k")
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userByValidationCheck, User.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при User.login c пробелами");
    }

    // name = null:
    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        userByValidationCheck = userByValidationCheck.toBuilder()
                .name(null)
                .build();

        final User newUser = userController.checkName(userByValidationCheck);

        assertEquals(userByValidationCheck.getLogin(), newUser.getName(), "Ошибка проверки "
                + "имени при User.name = null");
    }

    // User.birthday = текущая_дата + 1:
    @Test
    void shouldNot200StatusCodeWhenUserBirthdayIsFutureDay() {
        final LocalDate futureDay = LocalDate.now().plusDays(1);
        final URI uri = URI.create(URL + "/users");
        Integer responseStatusCode = 200;
        userByValidationCheck = userByValidationCheck.toBuilder()
                .birthday(futureDay)
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userByValidationCheck, User.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при "
                + "User.birthday = текущая_дата + 1");
    }
}