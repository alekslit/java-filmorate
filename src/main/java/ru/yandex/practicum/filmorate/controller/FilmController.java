package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.exception.IncorrectRequestParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.*;
import static ru.yandex.practicum.filmorate.exception.IncorrectRequestParameterException.*;

// класс контроллер для фильмов:
@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
        this.filmStorage = filmService.getFilmStorage();
    }

    // добавление Film:
    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmStorage.addFilm(film);
    }

    // обновление Film:
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmStorage.updateFilm(film);
    }

    // получение списка всех Film:
    @GetMapping
    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    // получение Film по id:
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        return filmStorage.getFilmById(id);
    }

    // User ставит лайк фильму:
    @PutMapping("/{id}/like/{userId}")
    public String addLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        checkId(id, PATH_VARIABLE_ID);
        checkId(userId, PATH_VARIABLE_USER_ID);

        return filmService.addLikeToFilm(id, userId);
    }

    // User удаляет лайк:
    @DeleteMapping("{id}/like/{userId}")
    public String removeLikeFromFilm(@PathVariable Long id, @PathVariable Long userId) {
        checkId(id, PATH_VARIABLE_ID);
        checkId(userId, PATH_VARIABLE_USER_ID);

        return filmService.removeLikeFromFilm(id, userId);
    }

    // получаем список топ фильмов по количеству лайков в размере {count}:
    @GetMapping("/popular")
    public List<Film> getTopFilmsForLikes(@RequestParam(defaultValue = "10", required = false) Integer count) {
        if (count <= 0) {
            log.debug("{}: " + INCORRECT_REQUEST_PARAM_MESSAGE + REQUEST_PARAM_COUNT + " = " + count,
                    IncorrectRequestParameterException.class.getSimpleName());
            throw new IncorrectRequestParameterException(INCORRECT_REQUEST_PARAM_MESSAGE + REQUEST_PARAM_COUNT,
                    REQUEST_PARAMETER_COUNT_ADVICE);
        }

        return filmService.getTopFilmsForLikes(count);
    }

    // вспомогательный метод для проверки id:
    public void checkId(Long id, String pathVariable) {
        if (id == null || id <= 0) {
            log.debug("{}: " + INCORRECT_PATH_VARIABLE_MESSAGE + pathVariable + " = " + id,
                    IncorrectPathVariableException.class.getSimpleName());
            throw new IncorrectPathVariableException(INCORRECT_PATH_VARIABLE_MESSAGE + pathVariable,
                    PATH_VARIABLE_ID_ADVICE);
        }
    }
}