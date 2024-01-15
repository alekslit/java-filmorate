package ru.yandex.practicum.filmorate.dao.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_USER_ID_ADVICE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_USER_ID_MESSAGE;

@Repository
@Slf4j
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // метод для добавления Event в БД:
    public static void addEvent(JdbcTemplate jdbcTemplate, EventType eventType,
                                EventOperation operation, Long userId, Long entityId) {
        // собираем Event:
        Event event = Event.builder()
                .eventType(eventType)
                .operation(operation)
                .userId(userId)
                .entityId(entityId)
                .timestamp(Instant.now().getEpochSecond())
                .build();

        // добавляем в БД:
        SimpleJdbcInsert eventInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("event_feed")
                .usingGeneratedKeyColumns("event_id");
        eventInsert.execute(eventToMap(event));
    }

    // получаем ленту событий пользователя:
    @Override
    public List<Event> getEventFeed(Long userId) {
        if (checkIfUserExists(userId)) {
            String sqlQuery =
                    "SELECT * " +
                    "FROM event_feed " +
                    "WHERE user_id = ?;";
            return jdbcTemplate.query(sqlQuery, this::mapRowToEvent, userId);
        } else {
            log.debug("{}: {}{}.", IllegalIdException.class.getSimpleName(), ILLEGAL_USER_ID_MESSAGE, userId);
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + userId, ILLEGAL_USER_ID_ADVICE);
        }
    }

    /*---Вспомогательные методы---*/
    private static Map<String, Object> eventToMap(Event event) {
        return Map.of(
                "event_type", event.getEventType(),
                "operation", event.getOperation(),
                "user_id", event.getUserId(),
                "entity_id", event.getEntityId(),
                "event_timestamp", Instant.ofEpochSecond(event.getTimestamp()));
    }

    private Event mapRowToEvent(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getLong("event_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(EventOperation.valueOf(resultSet.getString("operation")))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .timestamp(resultSet.getTimestamp("event_timestamp").getTime())
                .build();
    }

    private boolean checkIfUserExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM USERS " +
                                                     "WHERE user_id = ?", Integer.class, id);
        if (result == 0) {
            log.error("Пользователя с таким id {} нет", id);
            return false;
        }
        return true;
    }
}