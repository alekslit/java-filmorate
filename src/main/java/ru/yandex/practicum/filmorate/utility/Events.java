package ru.yandex.practicum.filmorate.utility;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/*---Утилитарный класс для методов объекта Event---*/
public class Events {

    private Events() {}

    // метод для добавления Event в БД:
    public static void addEvent(JdbcTemplate jdbcTemplate,
                                EventType eventType,
                                EventOperation operation,
                                Long userId,
                                Long entityId) {
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

    /*---Вспомогательные методы---*/
    private static Map<String, Object> eventToMap(Event event) {
        return Map.of(
                "event_type", event.getEventType(),
                "operation", event.getOperation(),
                "user_id", event.getUserId(),
                "entity_id", event.getEntityId(),
                "event_timestamp", Instant.ofEpochSecond(event.getTimestamp()));
    }

    public static Event mapRowToEvent(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getLong("event_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(EventOperation.valueOf(resultSet.getString("operation")))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .timestamp(resultSet.getTimestamp("event_timestamp").getTime())
                .build();
    }
}