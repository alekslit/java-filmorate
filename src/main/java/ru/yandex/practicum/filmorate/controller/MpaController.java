package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaDbService;

import java.util.List;

import static ru.yandex.practicum.filmorate.exception.IncorrectPathVariableException.*;

@RestController
@Slf4j
@RequestMapping("/mpa")
public class MpaController {
    private final MpaDbService mpaService;

    @Autowired
    public MpaController(MpaDbService mpaService) {
        this.mpaService = mpaService;
    }

    /*--------Получение списка всех MPA-рейтингов--------*/
    @GetMapping
    public List<Mpa> getAllMpa() {
        return mpaService.getAllMpa();
    }

    /*--------Получение Mpa по id--------*/
    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable Integer id) {
        checkId(id, PATH_VARIABLE_ID);
        return mpaService.getMpaById(id);
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