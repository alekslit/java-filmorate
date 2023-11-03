package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.AlreadyExistException.FILM_ALREADY_EXIST_MESSAGE;
import static ru.yandex.practicum.filmorate.exception.IllegalIdException.ILLEGAL_FILM_ID_MESSAGE;

// класс контроллер для фильмов:
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    // храним данные в памяти приложения:
    private final Map<Long, Film> films = new HashMap<>();
    private Long currentFilmIdNumber = 0L;


    // добавление фильма:
    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            log.debug("{}: " + FILM_ALREADY_EXIST_MESSAGE + film.getName(),
                    AlreadyExistException.class.getSimpleName());
            throw new AlreadyExistException(FILM_ALREADY_EXIST_MESSAGE + film.getName());
        }
        if (film.getId() != null) {
            log.debug("{}: " + ILLEGAL_FILM_ID_MESSAGE + film.getId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + film.getId());
        }
        film.setId(generateId());
        log.debug("Добавлен новый фильм: " + film.getName() + ", с id = " + film.getId());
        films.put(film.getId(), film);

        return film;
    }

    // обновление фильма:
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
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
            throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + film.getId());
        }

        return film;
    }

    // получение всех фильмов:
    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // генератор id:
    private Long generateId() {
        return ++currentFilmIdNumber;
    }

    // вспомогательный метод (получить фильм по id):
    public Film getFilmById(Long filmId) {
        if (films.get(filmId) == null) {
            log.debug("Нет Film с таким id = " + filmId);
            return null;
        }

        return films.get(filmId);
    }
}