package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.jsontypeadapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerTests {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private static final String URL = "http://localhost:8080";
    private static final URI USERS_URI = URI.create(URL + "/users");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

    private User userByValidationCheck;
    private HttpClient httpClient;
    private Integer responseStatusCode;

    private void init() {
        httpClient = HttpClient.newHttpClient();
        userByValidationCheck = User.builder()
                .email("obman@yandex.ru")
                .login("negodnik")
                .name("Smeshnoi")
                .birthday(LocalDate.of(1901, 1, 1))
                .build();

    }

    @BeforeEach
    void beforeEach() {
        init();
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