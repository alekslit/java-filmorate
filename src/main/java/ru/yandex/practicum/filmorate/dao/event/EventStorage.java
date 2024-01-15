package ru.yandex.practicum.filmorate.dao.event;

import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.List;

public interface EventStorage {
    List<Event> getEventFeed(Long userId);
}