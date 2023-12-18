package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.exception.IncorrectRequestParameterException;
import ru.yandex.practicum.filmorate.jsontypeadapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.InMemoryFilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

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
import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.INCORRECT_PATH_VARIABLE_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.PATH_VARIABLE_ID;
import static ru.yandex.practicum.filmorate.exception.IncorrectRequestParameterException.INCORRECT_REQUEST_PARAM_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IncorrectRequestParameterException.REQUEST_PARAM_COUNT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FilmControllerTests {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private static final String URL = "http://localhost:8080";
    private static final URI FILMS_URI = URI.create(URL + "/films");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

    private FilmController filmController;
    private Film film;
    private Film filmByValidationCheck;
    private HttpClient httpClient;
    private Integer responseStatusCode;
    private User userForLike;
    private InMemoryUserStorage userStorage;


    private void init() {
        userForLike = User.builder()
                .email("perelman@yandex.ru")
                .login("perelman")
                .name("genius")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();
        userStorage = new InMemoryUserStorage();
        filmController = new FilmController(new InMemoryFilmService(new InMemoryFilmStorage(), userStorage));
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
        userStorage.addUser(userForLike);
    }

    @BeforeEach
    void beforeEach() {
        init();
    }

    // Добавление нового фильма + получение списка всех фильмов:
    @Test
    void shouldAddNewFilmAndGetAllFilms() {
        final List<Film> filmsList = filmController.getAllFilms();

        assertNotNull(filmsList, "Список пуст, фильм не добавлен");
    }

    // Попытка добавить новый фильм, который уже добавлен:
    @Test
    void shouldAlreadyExistExceptionWhenNewFilmWithId() {
        film = film.toBuilder()
                .id(1L)
                .build();

        final AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> filmController.addFilm(film));

        assertEquals(FILM_ALREADY_EXIST_MESSAGE + film.getId(), exception.getMessage(), "Ошибка: "
                + "фильм добавлен повторно");
    }

    // Пробуем обновить фильм с существующим id:
    @Test
    void shouldUpdateFilm() {
        film = film.toBuilder()
                .description(film.getDescription() + " и не только!")
                .build();

        filmController.updateFilm(film);

        assertEquals(film.getDescription(), filmController.getFilmById(film.getId()).getDescription(), "Не "
                + "получилось обновить фильм");
    }

    // Пробуем обновить фильм c некорректным id:
    @Test
    void shouldGetIllegalExceptionWhenUpdateFilmWithIdIs777() {
        filmByValidationCheck = filmByValidationCheck.toBuilder()
                .id(777L)
                .build();

        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmController.updateFilm(filmByValidationCheck));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + filmByValidationCheck.getId(),
                exception.getMessage(), "Обновили фильм с некорректным id");
    }

    // Проверяем получение Film по id:
    @Test
    void shouldGetFilmById() {
        final Film filmById = filmController.getFilmById(1L);

        assertEquals(film, filmById, "Ошибка: получен некорректный объект Film.");
    }

    // Проверяем получение Film по id, которого не существует:
    @Test
    void shouldGetIllegalIdExceptionWhenGetFilmWithIdIs777() {
        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmController.getFilmById(777L));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + 777,
                exception.getMessage(), "Ошибка: смогли получить Film по несуществующему id.");
    }

    // Проверяем функцию лайка объекта Film (ставим лайк / удаляем лайк):
    @Test
    void shouldGetFilmWithLikeAndFilmWithoutLikeWhenLikeRemove() {
        filmController.addLikeToFilm(film.getId(), userForLike.getId());

        assertEquals(1, film.getAmountOfLikes(),
                "Ошибка: не получилось поставить лайк объекту Film.");

        filmController.removeLikeFromFilm(film.getId(), userForLike.getId());

        assertEquals(0, film.getAmountOfLikes(),
                "Ошибка: не получилось удалить лайк у объекта Film.");
    }

    // Проверяем функцию лайка объекта Film (получить топ фильмов / получить исключение если count отрицательный):
    @Test
    void checkGetTopFilmsList() {
        assertEquals(1, filmController.getTopFilmsForLikes(10).size(),
                "Ошибка: не удалось получить список фильмов.");

        final IncorrectRequestParameterException exception = assertThrows(
                IncorrectRequestParameterException.class,
                () -> filmController.getTopFilmsForLikes(-10));

        assertEquals(INCORRECT_REQUEST_PARAM_MESSAGE + REQUEST_PARAM_COUNT,
                exception.getMessage(), "Ошибка: метод работает с отрицательным count.");
    }

    // Проверяем получение Film по отрицательному id:
    @Test
    void shouldGetIncorrectPathVariableExceptionWhenFilmIdIsNegative() {
        final IncorrectPathVariableException exception = assertThrows(
                IncorrectPathVariableException.class,
                () -> filmController.getFilmById(-1L));

        assertEquals(INCORRECT_PATH_VARIABLE_MESSAGE + PATH_VARIABLE_ID,
                exception.getMessage(), "Ошибка: метод работает с отрицательным id.");
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