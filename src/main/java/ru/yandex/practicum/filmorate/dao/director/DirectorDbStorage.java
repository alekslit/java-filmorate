package ru.yandex.practicum.filmorate.dao.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.GENRE_INVALID_DATA_BASE_QUERY_ADVICE;
import static ru.yandex.practicum.filmorate.exception.InvalidDataBaseQueryException.INVALID_DATA_BASE_QUERY_MESSAGE;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public List<Director> getAllDirectors() {
        String query = "SELECT * FROM directors;";
        log.info("SELECT all Directors from DB");
        return jdbcTemplate.query(query, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> getByIdDirector(Integer id) {
        try {
            String query = "SELECT * FROM directors WHERE directors_id = ?;";
            log.info("SELECT request to DB Directors by id=" + id);

            return Optional.ofNullable(jdbcTemplate.queryForObject(query, this::mapRowToDirector, id));

        } catch (EmptyResultDataAccessException exception) {
            log.debug("{}: " + INVALID_DATA_BASE_QUERY_MESSAGE + " Размер ответа на запрос: "
                    + exception.getExpectedSize(), IllegalIdException.class.getSimpleName());
            throw new InvalidDataBaseQueryException(INVALID_DATA_BASE_QUERY_MESSAGE,
                    exception.getExpectedSize(),
                    GENRE_INVALID_DATA_BASE_QUERY_ADVICE);
        }
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
        if(getByIdDirector(director.getId()).isPresent()) {
            String query = "UPDATE directors SET name = ? WHERE directors_id = ?;";
            log.info("UPDATE request to DB Directors: " + director.getName());
            jdbcTemplate.update(query, director.getName(), director.getId());
        }

        return director;
    }

    @Override
    public void deleteDirector(Integer id) {
        String deleteQuery = "DELETE FROM directors WHERE directors_id = ?;";
        String updateQuery = "UPDATE directors SET directors_id = directors_id - 1 WHERE directors_id > ?;";

        // Удаление режиссера
        jdbcTemplate.update(deleteQuery, id);

        // Обновление последующих идентификаторов режиссеров
        jdbcTemplate.update(updateQuery, id);

    }
}