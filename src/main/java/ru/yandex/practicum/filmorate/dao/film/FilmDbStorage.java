package ru.yandex.practicum.filmorate.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.event.EventDbStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.director.Director;
import ru.yandex.practicum.filmorate.model.director.FilmDirector;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.genre.FilmGenre;
import ru.yandex.practicum.filmorate.model.genre.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;

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
            if (film.getGenres() != null) {
                saveGenres(film);
                sortGenres(film);
            }
            if (film.getDirectors() != null) {
                saveDirectors(film);
                sortDirectors(film);
            }
        } else if (getFilmById(film.getId()) != null) {
            log.debug("{}: {}{}", AlreadyExistException.class.getSimpleName(),
                    FILM_ALREADY_EXIST_MESSAGE, film.getId());
            throw new AlreadyExistException(FILM_ALREADY_EXIST_MESSAGE + film.getId(), FILM_ALREADY_EXIST_ADVICE);
        } else if (film.getId() != null) {
            log.debug("{}: {}{}", IllegalIdException.class.getSimpleName(), ILLEGAL_NEW_FILM_ID_MESSAGE, film.getId());
            throw new IllegalIdException(ILLEGAL_NEW_FILM_ID_MESSAGE + film.getId(), ILLEGAL_NEW_FILM_ID_ADVICE);
        }
        log.debug("Добавлен новый фильм: {}, c id = {}", film.getName(), film.getId());
        return film;
    }

    /*---Обновляем данные Film в БД---*/
    @Override
    public Film updateFilm(Film film) {
        if (checkIfFilmExists(film.getId())) {
            Film savedFilm = getFilmById(film.getId());
            jdbcTemplate.update(
                    "UPDATE FILMS SET name = ?, " +
                                     "release_date = ?, " +
                                     "description = ?, " +
                                     "duration = ?, " +
                                     "mpa_rating_id = ? " +
                    "WHERE film_id = ?;",
                    film.getName(),
                    film.getReleaseDate(),
                    film.getDescription(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
            if (!savedFilm.getGenres().equals(film.getGenres())) {
                jdbcTemplate.update("DELETE " +
                                    "FROM film_genres " +
                                    "WHERE film_id = ?;", film.getId());
                if (film.getGenres() != null) {
                    saveGenres(film);
                    sortGenres(film);
                } else {
                    film = film.toBuilder()
                            .genres(new HashSet<>())
                            .build();
                }
            }
            if (!savedFilm.getDirectors().equals(film.getDirectors())) {
                jdbcTemplate.update("DELETE " +
                                    "FROM film_directors " +
                                    "WHERE film_id = ?;", film.getId());
                if (film.getDirectors() != null) {
                    saveDirectors(film);
                    sortDirectors(film);
                } else {
                film = film.toBuilder()
                        .directors(new HashSet<>())
                        .build();
                }
            }
            log.debug("Обновлена информация о фильме: {}, c id = {}", film.getName(), film.getId());
        } else if (!checkIfFilmExists(film.getId())) {
            addFilm(film);
            log.debug("Добавлен новый фильм: {}, c id = {}", film.getName(), film.getId());
        }
        return film;
    }

    /*---Получить список всех Film---*/
    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, " +
                       "f.name, " +
                       "f.release_date, " +
                       "f.description, " +
                       "f.duration, " +
                      "mp.mpa_rating_id, " +
                      "mp.name AS mpa_name " +
                "FROM FILMS AS f " +
                "JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id);",
                getFilmMapper());
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    /*---Получить Film по id---*/
    @Override
    public Film getFilmById(Long filmId) {
        if (checkIfFilmExists(filmId)) {
            Film film = jdbcTemplate.queryForObject(
                    "SELECT f.film_id, " +
                           "f.name, " +
                           "f.release_date, " +
                           "f.description, " +
                           "f.duration, " +
                           "mp.mpa_rating_id, " +
                           "mp.name AS mpa_name " +
                    "FROM FILMS AS f " +
                    "JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                    "WHERE f.film_id = ?;", getFilmMapper(), filmId);
            setGenreForFilm(film);
            setDirectorsForFilm(film);
            return film;
        }
        log.error("фильм с id {} еще не добавлен.", filmId);
        throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikesOrYear(Long directorId, boolean sortByLikes) {
        if (checkIfDirectorExists(directorId)) {
            String orderByClause = sortByLikes ? "ORDER BY COUNT(fl.film_likes_id) ASC;" :
                    "ORDER BY f.release_date ASC;";
            List<Film> films = jdbcTemplate.query(
                    "SELECT f.film_id, " +
                            "f.name, " +
                            "f.release_date, " +
                            "f.description, " +
                            "f.duration, " +
                            "mp.mpa_rating_id, " +
                            "mp.name AS mpa_name " +
                    "FROM FILMS AS f " +
                    "LEFT OUTER JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                    "LEFT OUTER JOIN film_directors AS fd ON (f.film_id = fd.film_id) " +
                    "LEFT OUTER JOIN film_likes AS fl ON (f.film_id = fl.film_id) " +
                    "WHERE fd.directors_id = ? " +
                    "GROUP BY f.film_id " + orderByClause,
                    getFilmMapper(), directorId);
            setGenreForFilms(films);
            setDirectorForFilms(films);
            return films;
        }
        log.error("режиссер с id {} еще не добавлен.", directorId);
        throw new IllegalIdException(ILLEGAL_DIRECTOR_ID_MESSAGE + directorId, ILLEGAL_DIRECTOR_ID_ADVICE);
    }

    @Override
    public List<Film> searchFilmsByDirector(String query) {
        String searchQuery = "%" + query + "%";
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, " +
                       "f.name, " +
                       "f.release_date, " +
                       "f.description, " +
                       "f.duration, " +
                       "mp.mpa_rating_id, " +
                       "mp.name AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT OUTER JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                "LEFT OUTER JOIN film_likes AS fl ON (f.film_id = fl.film_id) " +
                "LEFT OUTER JOIN film_directors AS fd ON (f.film_id = fd.film_id) " +
                "LEFT OUTER JOIN directors AS d ON (fd.directors_id = d.directors_id) " +
                "WHERE UPPER(d.name) LIKE ? " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC;", getFilmMapper(),
                searchQuery.toUpperCase());
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        String searchQuery = "%" + query + "%";
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, " +
                       "f.name, " +
                       "f.release_date, " +
                       "f.description, " +
                       "f.duration, " +
                       "mp.mpa_rating_id, " +
                       "mp.name AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT OUTER JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                "LEFT OUTER JOIN film_likes AS fl ON (f.film_id = fl.film_id) " +
                "WHERE UPPER(f.name) LIKE ? " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC;", getFilmMapper(),
                searchQuery.toUpperCase());
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    @Override
    public List<Film> searchFilmsByTitleAndDirector(String query) {
        String searchQuery = "%" + query + "%";
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, " +
                       "f.name, " +
                       "f.release_date, " +
                       "f.description, " +
                       "f.duration, " +
                       "mp.mpa_rating_id, " +
                       "mp.name AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT OUTER JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                "LEFT OUTER JOIN film_likes AS fl ON (f.film_id = fl.film_id) " +
                "LEFT OUTER JOIN film_directors AS fd ON (f.film_id = fd.film_id) " +
                "LEFT OUTER JOIN directors AS d ON (fd.directors_id = d.directors_id) " +
                "WHERE UPPER(d.name) LIKE ? " +
                "OR UPPER(f.name) LIKE ? " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC;", getFilmMapper(),
                searchQuery.toUpperCase(), searchQuery.toUpperCase());
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    /*---Удалить фильм по id---*/
    @Override
    public void deleteFilmById(Long filmId) {
        if (checkIfFilmExists(filmId)) {
            jdbcTemplate.update("DELETE FROM FILMS " +
                                "WHERE film_id = ?;", filmId);
        } else {
            log.error("фильм с id {} еще не добавлен.", filmId);
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
        }
    }

    /*---Поставить лайк фильму---*/
    public String addLikeToFilm(Long id, Long userId) {
        if (checkIfFilmExists(id) && checkIfUserExists(userId)) {
            jdbcTemplate.update("INSERT INTO film_likes(film_id, user_id) " +
                                "VALUES(?, ?);", id, userId);
            // добавляем Event в БД:
            EventDbStorage.addEvent(jdbcTemplate, EventType.LIKE, EventOperation.ADD, userId, id);
        } else {
            log.debug("{}: {}{} и {}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_OBJECTS_ID_MESSAGE, id, userId);
            throw new IllegalIdException(ILLEGAL_OBJECTS_ID_MESSAGE, ILLEGAL_OBJECTS_ID_ADVICE);
        }
        return String.format("Пользователь с id: %d, поставил лайк фильму с id: %d.", userId, id);
    }

    /*---Удалить лайк---*/
    public String removeLikeFromFilm(Long id, Long userId) {
        if (checkIfFilmExists(id) && checkIfUserExists(userId)) {
            jdbcTemplate.update("DELETE FROM film_likes " +
                                "WHERE user_id = ? " +
                                "AND film_id = ?;", userId, id);
            // добавляем Event в БД:
            EventDbStorage.addEvent(jdbcTemplate, EventType.LIKE, EventOperation.REMOVE, userId, id);
        } else {
            log.debug("{}: {}{} и {}.", IllegalIdException.class.getSimpleName(),
                    ILLEGAL_OBJECTS_ID_MESSAGE, id, userId);
            throw new IllegalIdException(ILLEGAL_OBJECTS_ID_MESSAGE, ILLEGAL_OBJECTS_ID_ADVICE);
        }
        return String.format("Пользователь с id: %d, удалил свой лайк фильму с id: %d.", userId, id);
    }

    /*---Получить топ фильмов по популярности---*/
    public List<Film> getTopFilmsForLikes(Integer count) {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, " +
                       "f.name, " +
                       "f.release_date, " +
                       "f.description, " +
                       "f.duration, " +
                       "mp.mpa_rating_id, " +
                       "mp.name AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT OUTER JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                "LEFT OUTER JOIN film_likes AS fl ON (f.film_id = fl.film_id) " +
                "GROUP BY f.film_id, " +
                "fl.user_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?;",
                getFilmMapper(), count);
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbcTemplate.query(
                "SELECT fl1.film_id, " +
                       "f.name, " +
                       "f.description, " +
                       "f.release_date, " +
                       "f.duration, " +
                       "mr.mpa_rating_id, " +
                       "mr.name AS mpa_name " +
                "FROM films AS f " +
                "LEFT OUTER JOIN mpa_rating AS mr ON (f.mpa_rating_id = mr.mpa_rating_id) " +
                "JOIN film_likes AS fl1 ON (fl1.film_id = f.film_id) " +
                "JOIN film_likes AS fl2 ON (fl2.film_id = f.film_id) " +
                "JOIN film_likes AS fl3 ON (fl3.film_id = f.film_id) " +
                "WHERE fl1.user_id = ? " +
                  "AND fl2.user_id = ? " +
                "GROUP BY fl1.film_id " +
                "ORDER BY COUNT(fl1.USER_ID) DESC;", getFilmMapper(), userId, friendId);
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    /*---Получаем список топ фильмов по количеству лайков в размере {count},
    указанного жанра {genreId} за нужный год {year}---*/
    public List<Film> getTopFilmsForLikesWithYearAndGenreFilter(Integer count, Long genreId, Integer year) {
        // часть запроса на получение топа фильмов:
        String sglQuery =
                "SELECT f.film_id, " +
                       "f.name, " +
                       "f.release_date, " +
                       "f.description, " +
                       "f.duration, " +
                       "mp.mpa_rating_id, " +
                       "mp.name AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT OUTER JOIN mpa_rating AS mp ON (f.mpa_rating_id = mp.mpa_rating_id) " +
                "LEFT OUTER JOIN film_likes AS fl ON (f.film_id = fl.film_id) ";
        if (genreId == 0) {
            // топ с фильтром по году {year}:
            List<Film> films = jdbcTemplate.query(
                    sglQuery +
                    "WHERE EXTRACT (YEAR FROM CAST (f.release_date AS date)) = ? " +
                    "GROUP BY f.film_id, " +
                             "fl.user_id " +
                    "ORDER BY COUNT(fl.user_id) DESC " +
                    "LIMIT ?;",
                    getFilmMapper(), year, count);
            setGenreForFilms(films);
            setDirectorForFilms(films);
            return films;

        } else if (year == null) {
            // топ с фильтром по жанру {genreId}:
            List<Film> films = jdbcTemplate.query(
                    sglQuery +
                    "LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id) " +
                    "WHERE fg.genre_id = ? " +
                    "GROUP BY f.film_id, " +
                    "fl.user_id " +
                    "ORDER BY COUNT(fl.user_id) DESC " +
                    "LIMIT ?;",
                    getFilmMapper(), genreId, count);
            setGenreForFilms(films);
            setDirectorForFilms(films);
            return films;
        }

        // топ с фильтром по жанру {genreId} и году {year}:
        List<Film> films = jdbcTemplate.query(
                sglQuery +
                "LEFT OUTER JOIN film_genres AS fg ON (f.film_id = fg.film_id) " +
                "WHERE fg.genre_id = ? " +
                "AND EXTRACT (YEAR FROM CAST (f.release_date AS date)) = ? " +
                "GROUP BY f.film_id, " +
                         "fl.user_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?;", getFilmMapper(), genreId, year, count);
        setGenreForFilms(films);
        setDirectorForFilms(films);
        return films;
    }

    /*------Вспомогательные методы------*/
    private boolean checkIfFilmExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM FILMS " +
                                                     "WHERE film_id = ?;",
                Integer.class, id);
        if (result == 0) {
            log.error("фильм с id {} еще не добавлен.", id);
            return false;
        }
        return true;
    }

    private boolean checkIfUserExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM USERS " +
                                                     "WHERE user_id = ?;",
                Integer.class, id);
        if (result == 0) {
            log.error("Пользователя с таким id {} нет", id);
            return false;
        }
        return true;
    }

    private boolean checkIfDirectorExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM DIRECTORS " +
                                                     "WHERE directors_id = ?;", Integer.class, id);
        if (result == 0) {
            log.error("режиссера с таким id {} нет", id);
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
        jdbcTemplate.batchUpdate("INSERT INTO film_genres(film_id, genre_id) " +
                                 "VALUES (?, ?);",
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

    private void saveDirectors(Film film) {
        List<Director> directors = new ArrayList<>(film.getDirectors());
        jdbcTemplate.batchUpdate("INSERT INTO film_directors(film_id, directors_id) " +
                                 "VALUES (?, ?);",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, directors.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                }
        );
    }

    private void sortGenres(Film film) {
        HashSet<Genre> sortedHashSet = film.getGenres().stream()
                .sorted(this::compareGenre)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(sortedHashSet);
    }

    private void sortDirectors(Film film) {
        HashSet<Director> sortedHashSet = film.getDirectors().stream()
                .sorted(this::compareDirector)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setDirectors(sortedHashSet);
    }

    private int compareGenre(Genre genre1, Genre genre2) {
        return Integer.compare(genre1.getId(), genre2.getId());
    }

    private int compareDirector(Director director1, Director director2) {
        return Integer.compare(director1.getId(), director2.getId());
    }

    public void setGenreForFilms(List<Film> films) {
        Map<Long, List<FilmGenre>> filmGenreMap = getFilmGenreMap(getFilmsId(films));
        films.forEach(film -> film.setGenres(getGenresForFilm(film.getId(), filmGenreMap)));
        films.forEach(this::sortGenres);
    }

    public void setDirectorForFilms(List<Film> films) {
        Map<Long, List<FilmDirector>> filmDirectorsMap = getFilmDirectorsMap(getFilmsId(films));
        films.forEach(film -> film.setDirectors(getDirectorsForFilm(film.getId(), filmDirectorsMap)));
        films.forEach(this::sortDirectors);
    }

    private Map<Long, List<FilmGenre>> getFilmGenreMap(List<Long> filmsId) {
        List<FilmGenre> genres = getGenres(filmsId);
        return genres.stream()
                .collect(Collectors.groupingBy(FilmGenre::getFilmId));
    }

    private Map<Long, List<FilmDirector>> getFilmDirectorsMap(List<Long> filmsId) {
        List<FilmDirector> directors = getDirectors(filmsId);
        return directors.stream()
                .collect(Collectors.groupingBy(FilmDirector::getFilmId));
    }

    private List<FilmGenre> getGenres(List<Long> filmsId) {
        String filmsIdString = filmsId.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", " ", " "));
        return jdbcTemplate.query(
                "SELECT film_id, fg.genre_id, name " +
                "FROM film_genres AS fg " +
                "JOIN GENRE AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN(" + filmsIdString + ");", getFilmGenreMapper());
    }

    private List<FilmDirector> getDirectors(List<Long> filmsId) {
        String filmsIdString = filmsId.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", " ", " "));
        return jdbcTemplate.query(
                "SELECT film_id, fd.directors_id, name " +
                "FROM film_directors AS fd " +
                "JOIN directors AS d ON fd.directors_id = d.directors_id " +
                "WHERE fd.film_id IN(" + filmsIdString + ");", getFilmDirectorMapper());
    }

    private RowMapper<FilmGenre> getFilmGenreMapper() {
        return (rs, rowNum) -> new FilmGenre(
                rs.getInt("film_id"),
                rs.getInt("genre_id"),
                rs.getString("name")
        );
    }

    private RowMapper<FilmDirector> getFilmDirectorMapper() {
        return (rs, rowNum) -> new FilmDirector(
                rs.getLong("film_id"),
                rs.getInt("directors_id"),
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

    private Set<Director> getDirectorsForFilm(Long filmId, Map<Long, List<FilmDirector>> resultMapForFilm) {
        if (resultMapForFilm.get(filmId) == null) {
            log.error("Для фильма с id {} не указано режиссёров", filmId);
            return Collections.emptySet();
        }
        return resultMapForFilm.get(filmId).stream()
                .map(filmDirector -> new Director(filmDirector.getDirectorId(), filmDirector.getDirector()))
                .collect(Collectors.toSet());
    }

    public void setDirectorsForFilm(Film film) {
        List<FilmDirector> filmDirectorList = getDirectors(Collections.singletonList(film.getId()));
        if (filmDirectorList.isEmpty()) {
            log.error("Для фильма с id {} не указано режиссёров", film.getId());
            film.setDirectors(Collections.emptySet());
        } else {
            film.setDirectors(filmDirectorList.stream()
                    .map(filmDirector -> new Director(filmDirector.getDirectorId(), filmDirector.getDirector()))
                    .collect(Collectors.toSet()));
            sortDirectors(film);
        }
    }
}