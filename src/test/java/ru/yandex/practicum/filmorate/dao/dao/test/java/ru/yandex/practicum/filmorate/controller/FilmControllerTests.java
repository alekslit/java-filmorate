package ru.yandex.practicum.filmorate.dao.dao.test.java.ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.jsontypeadapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FilmControllerTests {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private static final String URL = "http://localhost:8080";
    private static final URI FILMS_URI = URI.create(URL + "/films");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

    private Film filmByValidationCheck;
    private HttpClient httpClient;
    private Integer responseStatusCode;

    private void init() {
        httpClient = HttpClient.newHttpClient();
        filmByValidationCheck = Film.builder()
                .name("Выстрел в пустоту")
                .description("О неожиданных поворотах в жизни")
                .releaseDate(LocalDate.of(2017, 6, 17))
                .duration(120)
                .build();
    }

    @BeforeEach
    void beforeEach() {
        init();
    }

    /*---Тесты валидации объекта Film---*/
    // поле name = null:
    @Test
    public void shouldGet400StatusCodeWhenFilmNameIsNull() throws IOException, InterruptedException {
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .name(null)
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(FILMS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при Film.name = null");
    }

    // description.length() = 201:
    @Test
    void shouldGet400StatusCodeWhenFilmDescriptionLengthIs201() throws IOException, InterruptedException {
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .description("12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890" + "1")
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(FILMS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при"
                + " Film.description.length() = 201");
    }

    // releaseDate < 1895-12-28:
    @Test
    void shouldGet400StatusCodeWhenFilmReleaseDateIsBeforeMovieBirthday() throws IOException, InterruptedException {
        final LocalDate beforeMovieBirthday = LocalDate.of(1895, 12, 27);
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .releaseDate(beforeMovieBirthday)
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(FILMS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при"
                + " Film.releaseDate < 1895-12-28");
    }

    // duration = 0:
    @Test
    void shouldGet400StatusCodeWhenFilmDurationIsZero() throws IOException, InterruptedException {
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .duration(0)
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(FILMS_URI)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        responseStatusCode = httpClient.send(httpRequest, handler).statusCode();

        assertEquals(400, responseStatusCode, "Ошибка валидации при"
                + " Film.duration = 0");
    }
}