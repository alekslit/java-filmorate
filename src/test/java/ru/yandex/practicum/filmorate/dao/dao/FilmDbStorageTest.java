package ru.yandex.practicum.filmorate.dao.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_FILM_ID_MESSAGE;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@JdbcTest
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;
    private Film film;
    private User userForLike;
    private Film filmForCheckTopList;

    public void init() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate);
        film = Film.builder()
                .name("test_film1_name")
                .description("test_film1_description")
                .releaseDate(LocalDate.of(1977, 7, 7))
                .duration(120)
                .rate(5)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
                .genres(List.of(Genre.builder()
                        .id(1)
                        .name("Комедия")
                        .build()))
                .build();
        userForLike = User.builder()
                .email("testuser1@tset.com")
                .login("test_user1_login")
                .name("test_user1_name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        filmForCheckTopList = Film.builder()
                .name("Не_попадёт_в_топ_до_выставления_лайков.")
                .description("test2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(100)
                .rate(4)
                .mpa(Mpa.builder()
                        .id(1)
                        .build())
                .build();
        // добавляем тестовый фильм:
        filmStorage.addFilm(film);
    }

    @BeforeEach
    public void setUp() {
        init();
    }

    @Test
    public void testGetFilmById() {
        Film savedFilm = filmStorage.getFilmById(film.getId());

        assertThat(savedFilm)
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @Test
    public void testUpdateFilm() {
        Film updateFilm = film.toBuilder()
                .description("test_update_description")
                .duration(180)
                .releaseDate(LocalDate.of(1999, 9, 9))
                .build();

        filmStorage.updateFilm(updateFilm);

        assertThat(updateFilm)
                .usingRecursiveComparison()
                .isEqualTo(filmStorage.getFilmById(film.getId()));
    }

    @Test
    public void testGetAllFilms() {
        List<Film> filmList = filmStorage.getAllFilms();

        assertThat(1).isEqualTo(filmList.size());
    }

    // проверяем функцию добавления лайка:
    @Test
    public void testAddLikeToFilm() {
        userStorage.addUser(userForLike);

        filmStorage.addLikeToFilm(film.getId(), userForLike.getId());

        assertThat(6).isEqualTo(filmStorage.getFilmById(film.getId()).getRate());
    }

    // проверяем функцию удаления лайка:
    @Test
    public void testRemoveLikeFromFilm() {
        userStorage.addUser(userForLike);
        filmStorage.addLikeToFilm(film.getId(), userForLike.getId());
        assertThat(6).isEqualTo(filmStorage.getFilmById(film.getId()).getRate());

        filmStorage.removeLikeFromFilm(film.getId(), userForLike.getId());


        assertThat(5).isEqualTo(filmStorage.getFilmById(film.getId()).getRate());
    }

    // проверяем функцию получения топ фильмов:
    @Test
    public void testGetTopFilmsForLikes() {
        filmStorage.addFilm(filmForCheckTopList);

        List<Film> topFilms = filmStorage.getTopFilmsForLikes(2);

        assertThat(film).isEqualTo(topFilms.get(0));
    }

    // Попытка добавить новый фильм, который уже добавлен:
    @Test
    void shouldAlreadyExistExceptionWhenNewFilmWithId() {
        final AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> filmStorage.addFilm(film));

        assertEquals(FILM_ALREADY_EXIST_MESSAGE + film.getId(), exception.getMessage(), "Ошибка: "
                + "фильм добавлен повторно");
    }

    // Пробуем обновить фильм c некорректным id:
    @Test
    void shouldGetIllegalExceptionWhenUpdateFilmWithIdIs777() {
        film = film.toBuilder()
                .id(777L)
                .build();

        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmStorage.updateFilm(film));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + film.getId(),
                exception.getMessage(), "Обновили фильм с некорректным id");
    }

    // Проверяем получение Film по id, которого не существует:
    @Test
    void shouldGetIllegalIdExceptionWhenGetFilmWithIdIs777() {
        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmStorage.getFilmById(777L));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + 777,
                exception.getMessage(), "Ошибка: смогли получить Film по несуществующему id.");
    }

    // Проверяем получение Film по отрицательному id:
    @Test
    void shouldGetIllegalIdExceptionWhenFilmIdIsNegative() {
        final IllegalIdException exception = assertThrows(
                IllegalIdException.class,
                () -> filmStorage.getFilmById(-1L));

        assertEquals(ILLEGAL_FILM_ID_MESSAGE + "-1",
                exception.getMessage(), "Ошибка: метод работает с отрицательным id.");
    }
}