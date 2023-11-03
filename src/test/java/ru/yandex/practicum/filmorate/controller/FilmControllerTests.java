package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.jsontypeadapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_FILM_ID_MESSAGE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FilmControllerTests {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private static final String URL = "http://localhost:8080";

    private FilmController filmController;
    private Film film;
    private Film filmByValidationCheck;
    private HttpClient httpClient;

    private void init() {
        filmController = new FilmController();
        httpClient = HttpClient.newHttpClient();
        film = Film.builder()
                .name("Аватар")
                .description("Фильм про синих людей....")
                .releaseDate(LocalDate.of(2009, 12, 10))
                .duration(162)
                .build();
        filmByValidationCheck = Film.builder()
                .name("Выстрел в пустоту")
                .description("О неожиданных поворотах в жизни")
                .releaseDate(LocalDate.of(2017, 6, 17))
                .duration(120)
                .build();
        filmController.addFilm(film);
    }

    @BeforeEach
    void beforeEach() {
        init();
    }

    // Добавление нового фильма + получение списка всех фильмов:
    @Test
     void shouldAddNewFilmAndGetAllFilms() {
        final List<Film> filmsList = filmController.getAllFilms();

        assertNotNull(filmsList, "Список пуст, фильм не добавлен (shouldAddNewFilmAndGetAllFilms)");
    }

    // Попытка добавить новый фильм, который уже добавлен:
    @Test
    void shouldAlreadyExistExceptionWhenNewFilmWithId() {
        film = film.toBuilder()
                .id(1L)
                .build();

        final AlreadyExistException exception =assertThrows(
                AlreadyExistException.class,
                () -> filmController.addFilm(film));

        assertEquals(FILM_ALREADY_EXIST_MESSAGE + film.getName(), exception.getMessage(), "Ошибка: "
                + "фильм добавлен повторно (shouldAlreadyExistExceptionWhenNewFilmWithId)");
    }

    // Пробуем обновить фильм с существующим id:
    @Test
    void shouldUpdateFilm() {
        film = film.toBuilder()
                .description(film.getDescription() + " и не только!")
                .build();

        filmController.updateFilm(film);

        assertEquals(film.getDescription(), filmController.getFilmById(film.getId()).getDescription(), "Не "
                + "получилось обновить фильм (shouldUpdateFilm)");
    }

    // Пробуем обновить фильм c некорректным id:
    @Test
    void shouldGetIllegalExceptionWhenFilmIdIs777() {
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .id(777L)
                .build();

        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmController.updateFilm(filmByValidationCheck));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + filmByValidationCheck.getId(),
                exception.getMessage(), "Обновили фильм с некорректным id " +
                        "(shouldGetIllegalExceptionWhenFilmIdIs777)");
    }

    /*---Тесты валидации объекта Film---*/
    // поле name = null:
    @Test
    public void shouldNot200StatusCodeWhenFilmNameIsNull() {
        final URI uri = URI.create(URL + "/films");
        Integer responseStatusCode = 200;
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .name(null)
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при Film.name = null");
    }

    // description.length() = 201:
    @Test
    void shouldNot200StatusCodeWhenFilmDescriptionLengthIs201() {
        final URI uri = URI.create(URL + "/films");
        Integer responseStatusCode = 200;
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .description("12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890" + "1")
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при"
                + " Film.description.length() = 201");
    }

    // releaseDate < 1895-12-28:
    @Test
    void shouldNot200StatusCodeWhenFilmReleaseDateIsBeforeMovieBirthday() {
        final LocalDate beforeMovieBirthday = LocalDate.of(1895, 12, 27);
        final URI uri = URI.create(URL + "/films");
        Integer responseStatusCode = 200;
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .releaseDate(beforeMovieBirthday)
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при"
                + " Film.releaseDate < 1895-12-28");
    }

    // duration = 0:
    @Test
    void shouldNot200StatusCodeWhenFilmDurationIsZero() {
        final URI uri = URI.create(URL + "/films");
        Integer responseStatusCode = 200;
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .duration(0)
                .build();
        final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(filmByValidationCheck, Film.class)))
                .build();

        try {
            responseStatusCode = httpClient.send(httpRequest, handler).statusCode();
        } catch (InterruptedException | IOException exception) {
            System.out.println("Во время работы приложения возникла ошибка" + exception.getMessage());
        }

        assertNotEquals(200, responseStatusCode, "Ошибка валидации при"
                + " Film.duration = 0");
    }
}