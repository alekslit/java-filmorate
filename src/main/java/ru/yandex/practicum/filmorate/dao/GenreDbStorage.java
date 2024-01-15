package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.genre.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_GENRE_ID_ADVICE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_GENRE_ID_MESSAGE;
import static ru.yandex.practicum.filmorate.query.SqlQuery.SQL_QUERY_GET_ALL_GENRES;
import static ru.yandex.practicum.filmorate.query.SqlQuery.SQL_QUERY_GET_GENRE_BY_ID;

@Slf4j
@Repository
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*---Получение жанра по его id---*/
    public Genre getGenreById(Integer genreId) {
        if (checkIfGenreExists(genreId)) {
            String sqlQuery = SQL_QUERY_GET_GENRE_BY_ID;
            Genre genre = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, genreId);

            return genre;
        }
        log.debug("{}: {}", IllegalIdException.class.getSimpleName(), ILLEGAL_GENRE_ID_MESSAGE + genreId);
        throw new IllegalIdException(ILLEGAL_GENRE_ID_MESSAGE + genreId, ILLEGAL_GENRE_ID_ADVICE);
    }

    /*---Получение списка всех жанров---*/
    public List<Genre> getAllGenres() {
        String sqlQuery = SQL_QUERY_GET_ALL_GENRES;
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    /*-------Вспомогательные методы-------*/
    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("name"))
                .build();

        return genre;
    }

    private boolean checkIfGenreExists(Integer id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM genre " +
                                                     "WHERE genre_id = ?;", Integer.class, id);
        if (result == 0) {
            log.error("жанр с id {} еще не добавлен.", id);
            return false;
        }
        return true;
    }
}