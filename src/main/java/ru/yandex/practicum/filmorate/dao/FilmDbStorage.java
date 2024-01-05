package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;
import static ru.yandex.practicum.filmorate.query.SqlQuery.*;

@Repository
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*---Добавляем фильм в БД---*/
    @Override
    public Film addFilm(Film film) {
        if (film.getId() == null) {
            SimpleJdbcInsert insertFilm = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("films")
                    .usingGeneratedKeyColumns("film_id");
            Long filmId = insertFilm.executeAndReturnKey(filmToMap(film)).longValue();
            film.setId(filmId);
            // связали id фильма и жанры:
            if (film.getGenres() != null) {
                saveGenres(film);
                sortGenres(film);
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
        if (checkIfFilmExists(film.getId())) {
            Film savedFilm = getFilmById(film.getId());
            jdbcTemplate.update(SQL_QUERY_UPDATE_FILM, film.getName(), film.getReleaseDate(), film.getDescription(), film.getDuration(),
                    film.getMpa().getId(), film.getId());
            if (!savedFilm.getGenres().equals(film.getGenres())) {
                jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
                if (film.getGenres() != null) {
                    saveGenres(film);
                    sortGenres(film);
                }
            }
            log.debug("Обновлена информация о фильме: " + film.getName() + ", с id = " + film.getId());
        } else if (!checkIfFilmExists(film.getId())) {
            addFilm(film);
            log.debug("Добавлен новый фильм: " + film.getName() + ", с id = " + film.getId());
        }
        return film;
    }

    /*---Получить список всех Film---*/
    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(
                SQL_QUERY_GET_ALL_FILMS, getFilmMapper());
        setGenreForFilms(films);
        return films;
    }

    /*---Получить Film по id---*/
    @Override
    public Film getFilmById(Long filmId) {
        if (checkIfFilmExists(filmId)) {
            Film film = jdbcTemplate.queryForObject(SQL_QUERY_GET_FILM_BY_ID, getFilmMapper(), filmId);
            setGenreForFilm(film);
            return film;
        }
        log.error("фильм с id {} еще не добавлен.", filmId);
        throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
    }

    /*---Удалить фильм по id---*/
    @Override
    public void deleteFilmById(Long filmId) {
        if (checkIfFilmExists(filmId)) {
            jdbcTemplate.update(SQL_QUERY_DELETE_FILM_BY_ID, filmId);
        } else {
            log.error("фильм с id {} еще не добавлен.", filmId);
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
        }
    }

    /*---Поставить лайк фильму---*/
    public String addLikeToFilm(Long id, Long userId) {
        if (checkIfFilmExists(id) && checkIfUserExists(userId)) {
            jdbcTemplate.update(SQL_QUERY_FILM_ADD_LIKE, id, userId);
        } else {
            log.error("Неверно указаны параметры запроса {}, {}", id, userId);
            throw new IllegalIdException(ILLEGAL_COMMON_ILM_ID_MESSAGE, ILLEGAL_FILM_ID_ADVICE);
        }
        return String.format("Пользователь с id: %d, поставил лайк фильму с id: %d.", userId, id);
    }

    /*---Удалить лайк---*/
    public String removeLikeFromFilm(Long id, Long userId) {
        if (checkIfFilmExists(id) && checkIfUserExists(userId)) {
            jdbcTemplate.update(SQL_QUERY_REMOVE_LIKE_FROM_FILM, userId, id);
        } else {
            log.error("Неверно указаны данные для удаления лайка: userId {}, id {}", userId, id);
            throw new IllegalIdException(ILLEGAL_COMMON_ILM_ID_MESSAGE, ILLEGAL_FILM_ID_ADVICE);
        }
        return String.format("Пользователь с id: %d, удалил свой лайк фильму с id: %d.", userId, id);
    }

    /*---Получить топ фильмов по популярности---*/
    public List<Film> getTopFilmsForLikes(Integer count) {
        List<Film> films = jdbcTemplate.query(SQL_QUERY_GET_TOP_FILMS_FOR_LIKES, getFilmMapper(), count);
        setGenreForFilms(films);
        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbcTemplate.query(SQL_QUERY_GET_COMMON_FILMS, getFilmMapper(), userId, friendId);
        setGenreForFilms(films);
        return films;
    }


    /*------Вспомогательные методы------*/
    private boolean checkIfFilmExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FILMS WHERE film_id = ?", Integer.class, id);
        if (result == 0) {
            log.error("фильм с id {} еще не добавлен.", id);
            return false;
        }
        return true;
    }

    private boolean checkIfUserExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS WHERE user_id = ?", Integer.class, id);
        if (result == 0) {
            log.error("Пользователя с таким id {} нет", id);
            return false;
        }
        return true;
    }

    private Map<String, Object> filmToMap(Film film) {
        return Map.of(
                "name", film.getName(),
                "description", film.getDescription(),
                "release_date", film.getReleaseDate(),
                "duration", film.getDuration(),
                "mpa_rating_id", film.getMpa().getId()
        );
    }

    public RowMapper<Film> getFilmMapper() {
        return (rs, rowNum) -> Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .mpa(Mpa.builder()
                        .id(rs.getInt("mpa_rating_id"))
                        .name(rs.getString("mpa_name"))
                        .build())
                .build();
    }

    public void saveGenres(Film film) {
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate("INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?);",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genres.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                }
        );
    }

    private void sortGenres(Film film) {
        HashSet<Genre> sortedHashSet = film.getGenres().stream()
                .sorted(this::compare)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(sortedHashSet);
    }

    private int compare(Genre genre1, Genre genre2) {
        return Integer.compare(genre1.getId(), genre2.getId());
    }

    private void setGenreForFilms(List<Film> films) {
        Map<Long, List<FilmGenre>> filmGenreMap = getFilmGenreMap(getFilmsId(films));
        films.forEach(film -> film.setGenres(getGenresForFilm(film.getId(), filmGenreMap)));
        films.forEach(this::sortGenres);
    }

    private Map<Long, List<FilmGenre>> getFilmGenreMap(List<Long> filmsId) {
        List<FilmGenre> genres = getGenres(filmsId);
        return genres.stream()
                .collect(Collectors.groupingBy(FilmGenre::getFilmId));
    }

    private List<FilmGenre> getGenres(List<Long> filmsId) {
        String filmsIdString = filmsId.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", " ", " "));
        return jdbcTemplate.query(
                "SELECT film_id, fg.genre_id, name " +
                        "FROM film_genres AS fg " +
                        "JOIN GENRE AS g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN(" + filmsIdString + ")", getFilmGenreMapper());
    }

    private RowMapper<FilmGenre> getFilmGenreMapper() {
        return (rs, rowNum) -> new FilmGenre(
                rs.getInt("film_id"),
                rs.getInt("genre_id"),
                rs.getString("name")
        );
    }

    private List<Long> getFilmsId(List<Film> films) {
        return films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
    }

    private Set<Genre> getGenresForFilm(Long filmId, Map<Long, List<FilmGenre>> resultMapForFilm) {
        if (resultMapForFilm.get(filmId) == null) {
            log.error("Для фильма с id {} не указано жанров", filmId);
            return Collections.emptySet();
        }
        return resultMapForFilm.get(filmId).stream()
                .map(filmGenre -> new Genre(filmGenre.getGenreId(), filmGenre.getGenre()))
                .collect(Collectors.toSet());
    }

    public void setGenreForFilm(Film film) {
        List<FilmGenre> filmGenreList = getGenres(Collections.singletonList(film.getId()));
        if (filmGenreList.isEmpty()) {
            log.error("Для фильма с id {} не указано жанров", film.getId());
            film.setGenres(Collections.emptySet());
        } else {
            film.setGenres(filmGenreList.stream()
                    .map(filmGenre -> new Genre(filmGenre.getGenreId(), filmGenre.getGenre()))
                    .collect(Collectors.toSet()));
            sortGenres(film);
        }
    }
}