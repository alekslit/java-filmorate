package ru.yandex.practicum.filmorate.dao.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.director.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_DIRECTOR_ID_ADVICE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_DIRECTOR_ID_MESSAGE;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    /*---Основные методы---*/
    @Override
    public List<Director> getAllDirectors() {
        String query =
                "SELECT * " +
                "FROM directors;";
        log.info("SELECT all Directors from DB");
        return jdbcTemplate.query(query, this::mapRowToDirector);
    }

    @Override
    public Director getByIdDirector(Integer id) {
        if (checkIfDirectorExists(id)) {
            String query =
                    "SELECT * " +
                    "FROM directors " +
                    "WHERE directors_id = ?;";
            log.info("SELECT request to DB Directors by id= {}", id);

            return jdbcTemplate.queryForObject(query, this::mapRowToDirector, id);
        }
        log.debug("{}: {}", IllegalIdException.class.getSimpleName(), ILLEGAL_DIRECTOR_ID_MESSAGE + id);
        throw new IllegalIdException(ILLEGAL_DIRECTOR_ID_MESSAGE + id, ILLEGAL_DIRECTOR_ID_ADVICE);
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert insertDirector = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("directors_id");
        Integer directorId = insertDirector.executeAndReturnKey(directorToMap(director)).intValue();
        director.setId(directorId);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        if (getByIdDirector(director.getId()) != null) {
            String query =
                    "UPDATE directors SET name = ? " +
                    "WHERE directors_id = ?;";
            log.info("UPDATE request to DB Directors: {}", director.getName());
            jdbcTemplate.update(query, director.getName(), director.getId());
        }

        return director;
    }

    @Override
    public void deleteDirector(Integer id) {
        String deleteQuery = "DELETE " +
                             "FROM directors " +
                             "WHERE directors_id = ?;";
        // Удаление режиссера
        jdbcTemplate.update(deleteQuery, id);
    }

    /*---Вспомогательные методы---*/
    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("directors_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    private static Map<String, Object> directorToMap(Director director) {
        return Map.of(
                "directors_id", director.getId(),
                "name", director.getName());
    }

    private boolean checkIfDirectorExists(Integer id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM directors " +
                                                     "WHERE directors_id = ?;", Integer.class, id);
        if (result == 0) {
            log.error("режиссёр с id {} еще не добавлен.", id);
            return false;
        }
        return true;
    }
}