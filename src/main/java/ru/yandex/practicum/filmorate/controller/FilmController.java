package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortBy;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.*;

// класс контроллер для фильмов:
@RestController
@Slf4j
@Validated
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    // добавление Film:
    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    // обновление Film:
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    // получение списка всех Film:
    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    // получение Film по id:
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteFilmById(@PathVariable Long id) {
        checkId(id, PATH_VARIABLE_ID);
        filmService.deleteFilmById(id);
        return String.format("фильм с id %d удален", id);
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
    public List<Film> getTopFilmsForLikes(
            @RequestParam(defaultValue = "10") @Positive(message = "Параметр запроса count, должен быть " +
                    "положительным числом.") Integer count,
            @RequestParam(defaultValue = "0") Long genreId,
            @RequestParam(required = false) @Min(value = 1895, message = "Параметр запроса year, не может быть " +
                    "меньше {value}.") Integer year) {
        // обычный топ фильмов:
        if (genreId == 0 && year == null) {
            return filmService.getTopFilmsForLikes(count);
        }

        // топ фильмов указанного жанра {genreId} за нужный год {year}:
        return filmService.getTopFilmsForLikesWithYearAndGenreFilter(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam("userId") Long userId, @RequestParam("friendId") Long friendId) {
        checkId(userId, PATH_VARIABLE_USER_ID);
        checkId(friendId, PATH_VARIABLE_FRIEND_ID);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        boolean sortByLikes = "likes".equalsIgnoreCase(sortBy);
        return filmService.getFilmsByDirectorSortedByLikesOrYear(directorId, sortByLikes);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam("query") String query,
                                  @RequestParam("by") SortBy by) {
        return filmService.searchFilmsByTitleOrDirector(query, by);
    }

    // вспомогательный метод для проверки id:
    public void checkId(Long id, String pathVariable) {
        if (id <= 0) {
            log.debug("{}: {} {} = {}", IncorrectPathVariableException.class.getSimpleName(),
                    INCORRECT_PATH_VARIABLE_MESSAGE, pathVariable, id);
            throw new IncorrectPathVariableException(INCORRECT_PATH_VARIABLE_MESSAGE + pathVariable,
                    PATH_VARIABLE_ID_ADVICE);
        }
    }
}