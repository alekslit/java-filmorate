package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.dao.GenreDbService;

import java.util.List;

import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.*;

@RestController
@Slf4j
@RequestMapping("/genres")
public class GenreController {
    private final GenreDbService genreService;

    @Autowired
    public GenreController(GenreDbService genreService) {
        this.genreService = genreService;
    }

    /*--------Получение списка всех Genre--------*/
    @GetMapping
    public List<Genre> getAllGenres() {
        return genreService.getAllGenres();
    }

    /*--------Получение Genre по id--------*/
    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Integer id) {
        checkId(id, PATH_VARIABLE_ID);
        return genreService.getGenreById(id);
    }

    // вспомогательный метод для проверки id:
    public void checkId(Integer id, String pathVariable) {
        if (id == null || id <= 0) {
            log.debug("{}: " + INCORRECT_PATH_VARIABLE_MESSAGE + pathVariable + " = " + id,
                    IncorrectPathVariableException.class.getSimpleName());
            throw new IncorrectPathVariableException(INCORRECT_PATH_VARIABLE_MESSAGE + pathVariable,
                    PATH_VARIABLE_ID_ADVICE);
        }
    }
}