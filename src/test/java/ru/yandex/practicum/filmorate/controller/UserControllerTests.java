package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.jsontypeadapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.InMemoryUserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

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
import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.INCORRECT_PATH_VARIABLE_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.PATH_VARIABLE_ID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerTests {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private static final String URL = "http://localhost:8080";
    private static final URI USERS_URI = URI.create(URL + "/users");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

    private UserController userController;
    private User user;
    private User userByValidationCheck;
    private HttpClient httpClient;
    private Integer responseStatusCode;
    private User userForFriend;
    private User userForCommonFriend;

    private void init() {
        userController = new UserController(new InMemoryUserService(new InMemoryUserStorage()));
        httpClient = HttpClient.newHttpClient();
        user = User.builder()
                .email("perelman@yandex.ru")
                .login("perelman")
                .name("genius")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();
        userForFriend = User.builder()
                .email("test1@test.ru")
                .login("test")
                .name("test")
                .birthday(LocalDate.now().minusDays(100))
                .build();
        userForCommonFriend = userForFriend.toBuilder()
                .email("test2@test.ru")
                .build();
        userByValidationCheck = User.builder()
                .email("obman@yandex.ru")
                .login("negodnik")
                .name("Smeshnoi")
                .birthday(LocalDate.of(1901, 1, 1))
                .build();
        userController.addUser(user);
        userController.addUser(userForFriend);
        userController.addUser(userForCommonFriend);
    }

    @BeforeEach
    void beforeEach() {
        init();
    }

    // Добавление нового User + получение списка всех User:
    @Test
    void shouldAddNewUserAndGetAllUsers() {
        final List<User> usersList = userController.getAllUsers();

        assertNotNull(usersList, "Список пуст, User не добавлен.");
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

        assertEquals(USER_ALREADY_EXIST_MESSAGE + user.getId(), exception.getMessage(),
                "Ошибка: User добавлен повторно.");
    }

    // Пробуем обновить User с существующим id:
    @Test
    void shouldUpdateUser() {
        user = user.toBuilder()
                .name("pereL")
                .build();

        userController.updateUser(user);

        assertEquals(user.getName(), userController.getUserById(user.getId()).getName(),
                "Не получилось обновить User");
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

        assertEquals(ILLEGAL_USER_ID_MESSAGE + userByValidationCheck.getId(), exception.getMessage(),
                "Обновили User с некорректным id");
    }

    // Проверяем получение User по id:
    @Test
    void shouldGetUserById() {
        final User userById = userController.getUserById(1L);

        assertEquals(user, userById, "Ошибка: получен некорректный объект User.");
    }

    // Проверяем получение User по id, которого не существует:
    @Test
    void shouldGetIllegalIdExceptionWhenGetUserWithIdIs777() {
        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> userController.getUserById(777L));

        assertEquals(ILLEGAL_USER_ID_MESSAGE + 777, exception.getMessage(),
                "Ошибка: смогли получить User по несуществующему id.");
    }

    // Проверяем функцию добавления в друзья для объекта User (добавляем в друзья -> получаем список друзей ->
    // -> получаем список общих -> удаляем из друзей):
    @Test
    void testFriendsFunction() {
        userController.addUserToFriends(user.getId(), userForFriend.getId());
        userController.addUserToFriends(user.getId(), userForCommonFriend.getId());
        userController.addUserToFriends(userForFriend.getId(), userForCommonFriend.getId());

        assertEquals(2, user.getFriends().size(), "Ошибка функции добавления в друзья.");
        assertEquals(2, userController.getAllFriendsList(user.getId()).size(),
                "Ошибка функции получения списка друзей.");
        assertEquals(1, userController.getCommonFriends(user.getId(), userForFriend.getId()).size(),
                "Ошибка функции получения списка общих друзей.");

        userController.removeUserFromFriends(user.getId(), userForFriend.getId());

        assertEquals(1, user.getFriends().size(),
                "Ошибка функции удаления из друзей.");
    }

    // Проверяем получение User по отрицательному id:
    @Test
    void shouldGetIncorrectPathVariableExceptionWhenUSerIdIsNegative() {
        final IncorrectPathVariableException exception = assertThrows(
                IncorrectPathVariableException.class,
                () -> userController.getUserById(-1L));

        assertEquals(INCORRECT_PATH_VARIABLE_MESSAGE + PATH_VARIABLE_ID,
                exception.getMessage(), "Ошибка: метод работает с отрицательным id.");
    }

    /*---Тесты валидации объекта User---*/
    // email без @:
    @Test
    void shouldGet400StatusCodeWhenUserEmailWithoutAt() throws IOException, InterruptedException {
        userByValidationCheck = userByValidationCheck.toBuilder()
                .email("withoutsobakayandex.ru")
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(USERS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userByValidationCheck, User.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при User.email без '@'");
    }

    // login с пробелами:
    @Test
    void shouldGet400StatusCodeWhenUserLoginWithBlankSpace() throws IOException, InterruptedException {
        userByValidationCheck = userByValidationCheck.toBuilder()
                .login("n e g o d n i k")
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(USERS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userByValidationCheck, User.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при User.login c пробелами");
    }

    // name = null:
    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        userByValidationCheck = userByValidationCheck.toBuilder()
                .name(null)
                .build();

        final User newUser = userController.addUser(userByValidationCheck);

        assertEquals(userByValidationCheck.getLogin(), newUser.getName(), "Ошибка проверки "
                + "имени при User.name = null");
    }

    // User.birthday = текущая_дата + 1:
    @Test
    void shouldGet400StatusCodeWhenUserBirthdayIsFutureDay() throws IOException, InterruptedException {
        final LocalDate futureDay = LocalDate.now().plusDays(1);
        userByValidationCheck = userByValidationCheck.toBuilder()
                .birthday(futureDay)
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(USERS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userByValidationCheck, User.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при "
                + "User.birthday = текущая_дата + 1");
    }
}