package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.FILM_INVALID_DATA_BASE_QUERY_ADVICE;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.INVALID_DATA_BASE_QUERY_MESSAGE;
import static ru.yandex.practicum.filmorate.query.SqlQuery.*;

@Repository
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private Set<Genre> sortedSet = new TreeSet(Comparator.comparing(Genre::getId));

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*---Добавляем фильм в БД---*/
    @Override
    public Film addFilm(Film film) {
        if (film.getId() == null) {
            // убираем дубликаты жанров:
            if (film.getGenres() != null) {
                sortedSet.clear();
                sortedSet.addAll(film.getGenres());
                film.setGenres(List.copyOf(sortedSet));
            }

            // добавили фильм:
            SimpleJdbcInsert insertFilm = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("films")
                    .usingGeneratedKeyColumns("film_id");
            Long filmId = insertFilm.executeAndReturnKey(filmToMap(film)).longValue();
            film.setId(filmId);

            // связали id фильма и жанры:
            if (film.getGenres() != null) {
                for (Genre genre: film.getGenres()) {
                    SimpleJdbcInsert insertFilmGenre = new SimpleJdbcInsert(jdbcTemplate)
                            .withTableName("film_genres")
                            .usingGeneratedKeyColumns("film_genres_id");
                    insertFilmGenre.execute(filmGenreToMap(film, genre));
                }
            }
        } else if (getFilmById(film.getId()) != null) {
            log.debug("{}: " + FILM_ALREADY_EXIST_MESSAGE + film.getId(),
                    AlreadyExistException.class.getSimpleName());
            throw new AlreadyExistException(FILM_ALREADY_EXIST_MESSAGE + film.getId(), FILM_ALREADY_EXIST_ADVICE);
        } else if (film.getId() != null) {
            log.debug("{}: " + ILLEGAL_NEW_FILM_ID_MESSAGE + film.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_NEW_FILM_ID_MESSAGE + film.getId(), ILLEGAL_NEW_FILM_ID_ADVICE);
        }

        log.debug("Добавлен новый фильм: " + film.getName() + ", с id = " + film.getId());
        return film;
    }

    /*---Обновляем данные Film в БД---*/
    @Override
    public Film updateFilm(Film film) {
        if (getFilmById(film.getId()) != null) {
            // убираем дубликаты жанров:
                if (film.getGenres() != null) {
                    sortedSet.clear();
                    sortedSet.addAll(film.getGenres());
                    film.setGenres(List.copyOf(sortedSet));
                }

            // обновили фильм:
            String filmSqlQuery = SQL_QUERY_UPDATE_FILM;
            jdbcTemplate.update(filmSqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getRate(),
                    film.getMpa().getId(),
                    film.getId());

            // обновили жанры:
            String filmGenreDeleteSqlQuery = "DELETE FROM film_genres " +
                                             "WHERE film_id = ?;";
            jdbcTemplate.update(filmGenreDeleteSqlQuery, film.getId());
            if (film.getGenres() != null) {
               for (Genre genre: film.getGenres()) {
                    SimpleJdbcInsert insertFilmGenre = new SimpleJdbcInsert(jdbcTemplate)
                            .withTableName("film_genres")
                            .usingGeneratedKeyColumns("film_genres_id");
                    insertFilmGenre.execute(filmGenreToMap(film, genre));
                }
            }
            log.debug("Обновлена информация о фильме: " + film.getName() + ", с id = " + film.getId());
        } else if (film.getId() == null) {
            addFilm(film);
            log.debug("Добавлен новый фильм: " + film.getName() + ", с id = " + film.getId());
        } else {
            log.debug("{}: " + ILLEGAL_FILM_ID_MESSAGE + film.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + film.getId(), ILLEGAL_FILM_ID_ADVICE);
        }

        return film;
    }

    /*---Получить список всех Film---*/
    @Override
    public List<Film> getAllFilms() {
        String sqlQuery = SQL_QUERY_GET_ALL_FILMS;
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    /*---Получить Film по id---*/
    @Override
    public Film getFilmById(Long filmId) {
        try {
            String sqlQuery = SQL_QUERY_GET_FILM_BY_ID;
            List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm,filmId);

            if (films.size() == 0) {
                log.debug("{}: " + ILLEGAL_FILM_ID_MESSAGE + filmId,
                        IllegalIdException.class.getSimpleName());
                throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
            }

            List<Genre> genres = new ArrayList<>();
            for (Film film: films) {
                genres.addAll(film.getGenres());
            }
            Film film = films.get(0);
            film.setGenres(genres);

            return film;
        } catch (EmptyResultDataAccessException exception) {
            log.debug("{}: " + INVALID_DATA_BASE_QUERY_MESSAGE + " Размер ответа на запрос: "
                    + exception.getExpectedSize(), IllegalIdException.class.getSimpleName());
            throw new InvalidDataBaseQueryException(INVALID_DATA_BASE_QUERY_MESSAGE,
                    exception.getExpectedSize(),
                    FILM_INVALID_DATA_BASE_QUERY_ADVICE);
        }
    }

    /*---Поставить лайк фильму---*/
    public String addLikeToFilm(Long id, Long userId) {
        // добавили запись о лайке:
        SimpleJdbcInsert insertLike = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film_likes")
                .usingGeneratedKeyColumns("film_likes_id");
        insertLike.execute(filmLikeToMap(id, userId));

        // увеличили rate фильма на 1:
        String sqlQuery = SQL_QUERY_FILM_RATE_PLUS;
        jdbcTemplate.update(sqlQuery, id);

        return String.format("Пользователь с id: %d, поставил лайк фильму с id: %d.", userId, id);
    }

    /*---Удалить лайк---*/
    public String removeLikeFromFilm(Long id, Long userId) {
        // удалили запись о лайке:
        String sqlQueryForLikeTable = SQL_QUERY_REMOVE_LIKE_FROM_FILM;
        jdbcTemplate.update(sqlQueryForLikeTable, id, userId);

        // уменьшили rate фильма на 1:
        String sqlQueryForFilmsTable = SQL_QUERY_FILM_RATE_MINUS;
        jdbcTemplate.update(sqlQueryForFilmsTable, id);

        return String.format("Пользователь с id: %d, удалил свой лайк фильму с id: %d.", userId, id);
    }

    /*---Получить топ фильмов по популярности---*/
    public List<Film> getTopFilmsForLikes(Integer count) {
        String sqlQuery = SQL_QUERY_GET_TOP_FILMS_FOR_LIKES;
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    /*------Вспомогательные методы------*/
    private static Map<String, Object> filmToMap(Film film) {
        if (film.getRate() == null) {
            return Map.of(
                    "name", film.getName(),
                    "description", film.getDescription(),
                    "release_date", film.getReleaseDate(),
                    "duration", film.getDuration(),
                    "mpa_rating_id",film.getMpa().getId());
        }

        return Map.of(
                "name", film.getName(),
                "description", film.getDescription(),
                "release_date", film.getReleaseDate(),
                "duration", film.getDuration(),
                "mpa_rating_id", film.getMpa().getId(),
                "rate", film.getRate());
    }

    private static Map<String, Object> filmLikeToMap(Long id, Long userId) {
            return Map.of(
                    "film_id", id,
                    "user_id", userId);

    }

    private static Map<String, Object> filmGenreToMap(Film film, Genre genre) {
        return Map.of(
                "film_id", film.getId(),
                "genre_id", genre.getId());
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .genres(new ArrayList<>())
                .build();

        // если есть rate:
        if (resultSet.getInt("rate") != 0) {
            film = film.toBuilder()
                    .rate(resultSet.getInt("rate"))
                    .build();
        }

        // если есть MPA:
        if (resultSet.getInt("mpa_rating_id") != 0) {
            film = film.toBuilder()
                    .mpa(Mpa.builder()
                            .id(resultSet.getInt("mpa_rating_id"))
                            .name(resultSet.getString("mpa_name"))
                            .build())
                    .build();
        }

        // если есть genres:
        if (resultSet.getInt("genre_id") != 0) {
            film = film.toBuilder()
                    .genres(List.of(Genre.builder()
                            .id(resultSet.getInt("genre_id"))
                            .name(resultSet.getString("genre_name"))
                            .build()))
                    .build();
        }

        return film;
    }
}