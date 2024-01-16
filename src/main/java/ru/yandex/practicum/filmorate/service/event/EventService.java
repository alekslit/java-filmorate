package ru.yandex.practicum.filmorate.service.event;

import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.List;

public interface EventService {
    List<Event> getEventFeed(Long userId);
}