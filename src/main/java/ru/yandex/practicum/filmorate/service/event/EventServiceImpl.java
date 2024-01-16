package ru.yandex.practicum.filmorate.service.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.event.EventDbStorage;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private final EventDbStorage eventStorage;

    @Autowired
    public EventServiceImpl(EventDbStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    @Override
    public List<Event> getEventFeed(Long userId) {
        return eventStorage.getEventFeed(userId);
    }
}