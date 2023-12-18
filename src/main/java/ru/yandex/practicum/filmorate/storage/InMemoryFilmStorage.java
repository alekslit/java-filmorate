package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_ADVICE;
import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long currentFilmIdNumber = 0L;

    @Override
    public Film addFilm(Film film) {
        if (films.containsKey(film.getId())) {
            log.debug("{}: " + FILM_ALREADY_EXIST_MESSAGE + film.getId(),
                    AlreadyExistException.class.getSimpleName());
            throw new AlreadyExistException(FILM_ALREADY_EXIST_MESSAGE + film.getId(), FILM_ALREADY_EXIST_ADVICE);
        }
        if (film.getId() != null) {
            log.debug("{}: " + ILLEGAL_NEW_FILM_ID_MESSAGE + film.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_NEW_FILM_ID_MESSAGE + film.getId(), ILLEGAL_NEW_FILM_ID_ADVICE);
        }
        film.setId(generateId());
        log.debug("Добавлен новый фильм: " + film.getName() + ", с id = " + film.getId());
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.debug("Обновлена информация о фильме: " + film.getName() + ", с id = " + film.getId());
        } else if (film.getId() == null) {
            film.setId(generateId());
            films.put(film.getId(), film);
            log.debug("Добавлен новый фильм: " + film.getName() + ", с id = " + film.getId());
        } else {
            log.debug("{}: " + ILLEGAL_FILM_ID_MESSAGE + film.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + film.getId(), ILLEGAL_FILM_ID_ADVICE);
        }

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // генератор id:
    public Long generateId() {
        return ++currentFilmIdNumber;
    }

    @Override
    public Film getFilmById(Long filmId) {
        if (films.get(filmId) == null) {
            log.debug("{}: " + ILLEGAL_FILM_ID_MESSAGE + filmId,
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
        }

        return films.get(filmId);
    }
}