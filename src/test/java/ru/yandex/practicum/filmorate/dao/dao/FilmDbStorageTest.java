package ru.yandex.practicum.filmorate.dao.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private DirectorStorage directorStorage;

    public void init() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate, filmStorage);
        directorStorage = new DirectorDbStorage(jdbcTemplate);
        Director director = new Director(1, "TestDirector");
        directorStorage.addDirector(director);
        film = Film.builder()
                .name("test_film1_name")
                .description("test_film1_description")
                .releaseDate(LocalDate.of(1977, 7, 7))
                .duration(120)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
                .genres(Set.of(Genre.builder()
                        .id(1)
                        .name("Комедия")
                        .build()))
                .directors(Set.of(Director.builder()
                        .id(director.getId())
                        .name(director.getName())
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
        Set<Genre> genresForTest = new HashSet<>();
        genresForTest.add(new Genre(1, "Комедия"));
        Film film = Film.builder()
                .id(1L)
                .name("name")
                .releaseDate(LocalDate.of(1967, 03, 25))
                .description("description")
                .duration(100)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
                .build();


        User user = User.builder()
                .id(1L)
                .email("mail@mail.ru")
                .login("dolore")
                .name("name")
                .birthday(LocalDate.of(1946, 8, 20))
                .build();

        jdbcTemplate.update("INSERT INTO FILMS(film_id, name, description, RELEASE_DATE, duration, mpa_rating_id) VALUES (?,?,?,?,?,?)", film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
        jdbcTemplate.update("INSERT INTO users(user_id, email, login, name, birthday) VALUES (?,?,?,?, ?)", user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        filmStorage.addLikeToFilm(film.getId(), user.getId());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes WHERE film_id = ?", Integer.class, film.getId());
        assertEquals(1, count);
    }

    // проверяем функцию удаления лайка:
    @Test
    public void testRemoveLikeFromFilm() {
        Set<Genre> genresForTest = new HashSet<>();
        genresForTest.add(new Genre(1, "Комедия"));
        Film film = Film.builder()
                .id(1L)
                .name("name")
                .releaseDate(LocalDate.of(1967, 03, 25))
                .description("description")
                .duration(100)
                .mpa(Mpa.builder()
                        .id(1)
                        .name("G")
                        .build())
                .build();


        User user = User.builder()
                .id(1L)
                .email("mail@mail.ru")
                .login("dolore")
                .name("name")
                .birthday(LocalDate.of(1946, 8, 20))
                .build();

        jdbcTemplate.update("INSERT INTO FILMS(film_id, name, description, RELEASE_DATE, duration, mpa_rating_id) VALUES (?,?,?,?,?,?)", film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
        jdbcTemplate.update("INSERT INTO USERS(user_id, email, login, name, birthday) VALUES (?,?,?,?, ?)", user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        jdbcTemplate.update("INSERT INTO film_likes(user_id, film_id) VALUES(?, ?)", user.getId(), film.getId());

        filmStorage.removeLikeFromFilm(film.getId(), user.getId());
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes WHERE film_id = ?", Integer.class, film.getId()));
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