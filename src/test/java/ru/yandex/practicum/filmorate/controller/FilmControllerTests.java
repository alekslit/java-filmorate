package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.controller.FilmController.MOVIE_BIRTHDAY;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_FILM_ID_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.ValidationException.*;

@SpringBootTest
public class FilmControllerTests {
    private FilmController filmController;
    private Film film;

    private void init() {
        filmController = new FilmController();
        film = Film.builder()
                .name("Аватар")
                .description("Фильм про синих людей....")
                .releaseDate(LocalDate.of(2009, 12, 10))
                .duration(162)
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
        film = Film.builder()
                .id(777L)
                .name("Выстрел в пустоту")
                .description("О неожиданных поворотах в жизни")
                .releaseDate(LocalDate.of(2017, 6, 17))
                .duration(120)
                .build();

        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmController.updateFilm(film));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + film.getId(), exception.getMessage(), "Обновили "
                + "фильм с некорректным id (shouldGetIllegalExceptionWhenFilmIdIs777)");
    }

    /*---Проверяем работу валидации объектов Film---*/
    @Test
    void shouldValidationExceptionWhenFilmNameIsNull() {
        film = film.toBuilder()
                .name(null)
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film));

        assertEquals(INVALID_FILM_NAME_MESSAGE, exception.getMessage(), "Некорректный"
                + " объект Film прошёл валидацию (shouldValidationExceptionWhenFilmNameIsNull)");
    }

    @Test
    void shouldValidationExceptionWhenFilmDescriptionLengthIs201() {
        final String twoHundredOneLength = "12345678901234567890123456789012345678901234567890"
                + "12345678901234567890123456789012345678901234567890"
                + "12345678901234567890123456789012345678901234567890"
                + "12345678901234567890123456789012345678901234567890" + "1";
        film = film.toBuilder()
                .description(twoHundredOneLength)
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film));

        assertEquals(ILLEGAL_FILM_DESCRIPTION_LENGTH_MESSAGE, exception.getMessage(), "Некорректный"
                + " объект Film прошёл валидацию (shouldValidationExceptionWhenFilmDescriptionLengthIs201)");
    }

    @Test
    void shouldValidationExceptionWhenFilmReleaseDateIsBeforeMovieBirthday() {
        final LocalDate beforeMovieBirthday = LocalDate.of(1895, 12, 27);
        film = film.toBuilder()
                .releaseDate(beforeMovieBirthday)
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film));

        assertEquals(ILLEGAL_FILM_RELEASE_DATE_MESSAGE + MOVIE_BIRTHDAY,
                exception.getMessage(), "Некорректный объект Film прошёл валидацию "
                        + "(shouldValidationExceptionWhenFilmReleaseDateIsBeforeMovieBirthday)");
    }

    @Test
    void shouldValidationExceptionWhenFilmDurationIsZero() {
        film = film.toBuilder()
                .duration(0)
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film));

        assertEquals(ILLEGAL_FILM_DURATION_MESSAGE, exception.getMessage(), "Некорректный объект Film прошёл "
                + "валидацию (shouldValidationExceptionWhenFilmDurationIsZero)");
    }
}