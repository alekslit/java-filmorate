package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@JdbcTest
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;
    private Film film;
    private User userForLike;
    private Film filmForCheckLikeFunctional;

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
        // поставим фильму rate 4 он не дожен быть в топе, до добавления лайков:
        filmForCheckLikeFunctional = Film.builder()
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

    @Test
    public void testLikeAndRateFunctional() {
        /* сравним два фильма после выставления 2-ух лайков, чтобы убедиться, что rate увеличился
         и положение фильмов в топе изменилось, затем проверим, что работает удаление лайков*/
        // без лайков:
        filmStorage.addFilm(filmForCheckLikeFunctional);
        userStorage.addUser(userForLike);

        assertThat(filmStorage.getFilmById(film.getId()))
                .isEqualTo(filmStorage.getTopFilmsForLikes(1).get(0));

        // ставим лайки:
        filmStorage.addLikeToFilm(filmForCheckLikeFunctional.getId(), userForLike.getId());
        filmStorage.addLikeToFilm(filmForCheckLikeFunctional.getId(), userForLike.getId());

        assertThat(filmStorage.getFilmById(filmForCheckLikeFunctional.getId()))
                .isEqualTo(filmStorage.getTopFilmsForLikes(1).get(0));

        // удаляем лайки:
        filmStorage.removeLikeFromFilm(filmForCheckLikeFunctional.getId(), userForLike.getId());
        filmStorage.removeLikeFromFilm(filmForCheckLikeFunctional.getId(), userForLike.getId());

        assertThat(filmStorage.getFilmById(film.getId()))
                .isEqualTo(filmStorage.getTopFilmsForLikes(1).get(0));
    }
}