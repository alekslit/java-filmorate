package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.InvalidDataBaseQuery;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQuery.INVALID_DATA_BASE_QUERY_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQuery.MPA_INVALID_DATA_BASE_QUERY_ADVICE;
import static ru.yandex.practicum.filmorate.query.SqlQuery.SQL_QUERY_GET_ALL_MPA;
import static ru.yandex.practicum.filmorate.query.SqlQuery.SQL_QUERY_GET_MPA_BY_ID;

@Repository
@Slf4j
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*---Получение MPA-рейтинга по его id---*/
    public Mpa getMpaById(Integer mpaId) {
        try {
            String sqlQuery = SQL_QUERY_GET_MPA_BY_ID;
            Mpa mpa = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMpa, mpaId);

            return mpa;
        } catch (EmptyResultDataAccessException exception) {
            log.debug("{}: " + INVALID_DATA_BASE_QUERY_MESSAGE + " Размер ответа на запрос: "
                    + exception.getExpectedSize(), IllegalIdException.class.getSimpleName());
            throw new InvalidDataBaseQuery(INVALID_DATA_BASE_QUERY_MESSAGE,
                    exception.getExpectedSize(),
                    MPA_INVALID_DATA_BASE_QUERY_ADVICE);
        }
    }

    /*---Получение списка всех MPA-рейтингов---*/
    public List<Mpa> getAllMpa() {
        String sqlQuery = SQL_QUERY_GET_ALL_MPA;
        return jdbcTemplate.query(sqlQuery, this::mapRowToMpa);
    }

    /*-------Вспомогательные методы-------*/
    private Mpa mapRowToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        Mpa mpa = Mpa.builder()
                .id(resultSet.getInt("mpa_rating_id"))
                .name(resultSet.getString("name"))
                .build();

        return mpa;
    }
}