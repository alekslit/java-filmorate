package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Event {
    // уникальный идентификатор объекта:
    private Long eventId;
    // тип события (LIKE, REVIEW или FRIEND):
    private EventType eventType;
    // название операции (что сделали?) (REMOVE, ADD, UPDATE):
    private EventOperation operation;
    // id пользователя (кто сделал?):
    private Long userId;
    // id сущности с которой произошло событие:
    private Long entityId;
    // тайм-штамп события:
    private Long timestamp;
}